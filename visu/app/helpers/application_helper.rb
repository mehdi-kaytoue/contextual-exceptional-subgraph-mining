module ApplicationHelper


  def self.load_pattern_at(filename, index, is_map = false)

    i = 0
    file = File.new( filename )
    nodes = []
    links = []
    data = {}
    if is_map

      stations_data = {}
      station_trend = {}
      stations = File.new( Rails.root.join('public', 'data_from_stations_with_geo.csv') )
      while line = stations.gets do
        line.scrub.delete!( "\n" )
        parts = line.scrub.split(';')
        station_trend[ parts[0] ] = []

        (16..(parts.size - 1)).each {|i| station_trend[ parts[0] ] << parts[i].delete("\n")  }
        stations_data[parts[0]] = "#{parts[7]}/#{parts[8]}/#{ parts[2]}@#{parts[9]}"
      end
    end

    while (line = file.gets) do
      if i < index
        i+= 1
        next
      end
      line.delete!( "\n" )
      parts = line.scan(/[^\t]+/).to_a
      relations = parts[0].scan(/[^:]+/).to_a

      relations.each do |r|
        words = r.scan(/[^;]+/).to_a

        if !nodes.any? {|h| h[:name] == words[0] }
          data_iris = $redis.get( stations_data[words[0]].split('@').last)



          puts "================== #{stations_data[words[0]].split('@').last} =================="




          iris = eval(data_iris)  unless data_iris.nil?
          nodes << {:name =>  stations_data[words[0]], :type => 2, :iris => iris, :iris_trend => station_trend[words[0]]} if is_map
          nodes << {:name =>  words[0], :type => 2} if !is_map
        end

        if !nodes.any? {|h| h[:name] == words[1] }


          data_redis = $redis.get( stations_data[words[1]].split('@').last)

          iris = eval(data_redis) unless data_redis.nil?
          nodes << {:name =>  stations_data[words[1]], :type => 2, :iris => iris, :iris_trend => station_trend[words[1]]} if is_map
          nodes << {:name =>  words[1], :type => 2} if !is_map
        end


        if is_map
          n1_index = nodes.index {|h| h[:name] ==  stations_data[words[0]] } # => 0
          n2_index = nodes.index {|h| h[:name] ==  stations_data[words[1]] } # => 0
        else
          n1_index = nodes.index {|h| h[:name] == words[0] } # => 0
          n2_index = nodes.index {|h| h[:name] == words[1] } # => 0
        end



        uniq_edge_key = "#{ words[0] }/#{ words[1] }"

        unless data.has_key?(uniq_edge_key)

          key = words[0]
          key = stations_data[words[0]] if is_map


          links << {
              'source' =>  n1_index,
              'target'=> n2_index,
              'name' => "#{key}",
              'value' => 10,
              'distance' =>  5,
              'supp_ctx' => words[2],
              'supp_all' => words[3],
              'q' => words[4]
          }
          data[uniq_edge_key] = true
        end
      end
    return {:nodes => nodes, :links => links}
    end
  end

  def self.load_patterns_info(filename, has_header = false, is_map = false )

    patterns = []
    file = File.new( filename )
    header = file.gets if has_header

    while (line = file.gets) do



      next if line == '' || line == "\n"






      line.delete!( "\n" )


      parts = line.scan(/[^\t]+/).to_a



      relations = parts[0].scan(/[^:]+/).to_a

      list = {}


      relations.each do |r|







        words = r.scan(/[^;]+/).to_a







        next if (words[0].nil? || words[0] == "\n") || (words[1].nil? || words[1] == "\n")


        #
        # if is_map
        #   if list.has_key?( stations_data[words[0]] )
        #     list[stations_data[words[0]]] << stations_data[words[1]]
        #
        #   else
        #     list[stations_data[words[0]]] = [ stations_data[words[1]] ]
        #   end
        # else
        if list.has_key?( words[0] )
          list[words[0]] << words[1]

        else
          list[words[0]] = [ words[1] ]
        end
        # end

      end



      pattern = {links: list, attrs: [ parts[1].to_f.round(2), parts[2], parts[3], parts[4], parts[5] ] }
      parts[6].split(';').each {|el| pattern[:attrs] << el.split('=').last.to_f.round(2) } unless parts[6].nil?



      test = {}
      parts[parts.size - 1].scan(/[^;]+/).to_a.each{|el| p = el.scan(/[^=]+/); test[p[0]] = p[1] if p[1] != '*'}
      pattern[:attrs] << test


      patterns << pattern


    end





    return patterns
  end
  def self.load_graph(patterns, filename, has_head = true, has_header = true)
    data = {}
    nodes = []
    links = []

    t1 = Time.now

    file = File.new( filename )
    while (line = file.gets) do

      if has_header then has_header = false; next end
      # if has_header then has_head = false; next end

      words = line.split(';')
      _in = words[words.size - 1].delete("\n")
      _out = words[words.size - 2].delete("\n")


      if !nodes.any? {|h| h[:name] == _in }
        nodes << {:name => _in, :type => 2}
      end


      if !nodes.any? {|h| h == {:name => _out, :type => 2}}
        nodes << {:name => _out, :type => 2}
      end

      n1_index = nodes.index {|h| h[:name] == _in } # => 0
      n2_index = nodes.index {|h| h[:name] == _out } # => 0

      uniq_edge_key = "#{ _in }/#{ _out }"

      unless data.has_key?(uniq_edge_key)
        links << {
            'source' =>  n1_index,
            'target'=> n2_index,
            'name' => "#{_in}",
            'value' => 10,
            'distance' =>  5
        }
        data[uniq_edge_key] = true
      end
    end



    return {:nodes => nodes, :links => links}
  end
  def self.find_pattern_index( patterns, key )
    patterns.each_with_index do |pattern, index|
      if pattern[:links].keys.include?( key )
        return index
      end
    end

    return nil
  end
  def self.load_graph_from_pattern( filename, has_header = false, is_map = false )
    file = File.new( filename )
    data = {}
    nodes = []
    links = []




    if is_map

      stations_data = {}
      stations = File.new( Rails.root.join('public', 'stations.csv') )
      while line = stations.gets do
        line.delete!( "\n" )
        parts = line.split(',')
        stations_data[parts[0].tr!('"', '')] = "#{parts[7].tr!('"', '')}/#{parts[8].tr!('"', '')}/#{ parts[2].tr!('"', '') }"
      end
    end



    file.gets if has_header

    while (line = file.gets) do
      next if line == '' || line == "\n"


      line.delete!( "\n" )


      parts = line.scan(/[^\t]+/).to_a

      relations = parts[0].scan(/[^:]+/).to_a


      relations.each do |r|




        words = r.scan(/[^;]+/).to_a


        if !nodes.any? {|h| h[:name] == words[0] }
          nodes << {:name =>  stations_data[words[0]], :type => 2} if is_map
          nodes << {:name =>  words[0], :type => 2} if !is_map
        end

        if !nodes.any? {|h| h[:name] == words[1] }
          nodes << {:name =>  stations_data[words[1]], :type => 2} if is_map
          nodes << {:name =>  words[1], :type => 2} if !is_map
        end


        if is_map
          n1_index = nodes.index {|h| h[:name] ==  stations_data[words[0]] } # => 0
          n2_index = nodes.index {|h| h[:name] ==  stations_data[words[1]] } # => 0
        else
          n1_index = nodes.index {|h| h[:name] == words[0] } # => 0
          n2_index = nodes.index {|h| h[:name] == words[1] } # => 0
        end



        uniq_edge_key = "#{ words[0] }/#{ words[1] }"

        unless data.has_key?(uniq_edge_key)

          key = words[0]
          key = stations_data[words[0]] if is_map


          links << {
              'source' =>  n1_index,
              'target'=> n2_index,
              'name' => "#{key}",
              'value' => 10,
              'distance' =>  5
          }
          data[uniq_edge_key] = true
        end
      end
    end


    return {:nodes => nodes, :links => links}
  end
  def self.load_patterns( filename, has_header = false, is_map = false)

    if is_map

      stations_data = {}
      stations = File.new( Rails.root.join('public', 'stations.csv') )
      while line = stations.gets do
        line.delete!( "\n" )
        parts = line.split(',')
        stations_data[parts[0].tr!('"', '')] = "#{parts[7].tr!('"', '')}/#{parts[8].tr!('"', '')}/#{ parts[2].tr!('"', '') }"
      end
    end






    patterns = []
    file = File.new( filename )
    while (line = file.gets) do


      if has_header then has_header = false; next end
      next if line == '' || line == "\n"



      line.delete!( "\n" )


      parts = line.scan(/[^\t]+/).to_a



      relations = parts[0].scan(/[^:]+/).to_a

      list = {}


      relations.each do |r|







        words = r.scan(/[^;]+/).to_a







        next if (words[0].nil? || words[0] == "\n") || (words[1].nil? || words[1] == "\n")



        if is_map
          if list.has_key?( stations_data[words[0]] )
            list[stations_data[words[0]]] << stations_data[words[1]]

          else
            list[stations_data[words[0]]] = [ stations_data[words[1]] ]
          end
        else
          if list.has_key?( words[0] )
            list[words[0]] << words[1]

          else
            list[words[0]] = [ words[1] ]
          end
        end




      end



      pattern = {links: list, attrs: [ parts[1], parts[2], parts[3], parts[4], parts[5] ] }
      parts[6].split(';').each {|el| pattern[:attrs] << el.split('=').last } unless parts[6].nil?






      patterns << pattern


    end





    return patterns
  end

end
