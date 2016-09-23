class WelcomeController < ApplicationController
  before_filter :ensure_params, only: [:show]


  def welcome


    render 'layouts/welcome'
  end


  def index

    # 1. Load Public/results directory to list the Xp
    @results = {}
    @errors = []
    folder_path = Rails.root.join( 'public', 'results' ).to_s
    Dir.chdir( folder_path )

    begin
      Dir.glob('*').select {|f|
        if File.directory? f then
          begin
            @results[f] = {link: f, path:"#{ folder_path }/#{ f }", type:  JSON.load( File.open("#{folder_path }/#{ f }" + '/config.json').read )['type']}
          rescue => e
            @errors << "The XP '#{ f }' has been skipped"
            next
          end
        end
      }
    rescue => e


      Dir.glob('*').select do |f|
        if File.directory? f
          unless (File.exist?( "#{folder_path}/#{f}/data.csv" ) && File.exist?( "#{folder_path}/#{f}/patterns-output.csv" ) && File.exist?( "#{folder_path}/#{f}/config.json" ) )
            path = Rails.root.join('public', 'results', f)
           # FileUtils.rm_r path
            @errors << "The XP '#{ f }' has been removed because not respecting file structure."
          end
        end
      end
      retry
    end

    @results = @results.map{|k, v| [ DateTime.strptime(k.split('_')[1], '%s').strftime('%F_%H:%M:%S '), v ] }.to_h

    @results = @results.sort_by{|k, _| k}.reverse.to_h


    @type = 'application'
    @graph = nil
    # 2. FE: List on a table each Xp Patterns
    if params[:xp]
      path = Rails.root.join('public', 'results', params[:xp])
      json = JSON.load( File.open(path.to_s + '/config.json').read )

      @complexity = json['complex'] if json.has_key?( 'complex' )
      @type = json['type']
      @current = params[:xp]

      @patterns = ApplicationHelper.load_patterns( path.to_s + '/patterns-output.csv', false,  @type == 'map' ) unless @complexity.nil?


      unless @complexity.nil?
        @graph = ApplicationHelper.load_graph(@patterns, path.to_s + '/data.csv' ) unless @complexity
        @graph = ApplicationHelper.load_graph_from_pattern( path.to_s + '/patterns-output.csv', false, @type == 'map' ) if @complexity
      else
        @patterns = ApplicationHelper.load_patterns_info( path.to_s + '/patterns-output.csv', false,  @type == 'map'  )
        @data = []
      end


    end


    unless @patterns.nil?
      @uniq_key = {}
      @patterns.each_with_index do |node, index|
        key = Digest::SHA256.base64digest( node[:attrs].last.map{|k,v| "#{k}=#{v}"}.join('&')  )
        node['signature'] = key

        if @uniq_key.has_key? key
          @uniq_key[key][:count] += 1
        else
          @uniq_key[key] = {:count => 0, :index => []}
        end

        @uniq_key[key][:index] <<  index
      end
      @uniq_key = @uniq_key.sort_by{|k,v| v[:count]}.to_h
    end




    case @type
      when 'application'
        render 'index', layout: 'application'
      when 'game'
        render 'game', layout: 'application'
      else
        render 'index', layout: 'application'
    end




  end


  def get_pattern
    path = Rails.root.join('public', 'results', params[:xp])
    @data = ApplicationHelper.load_pattern_at( path.to_s + '/patterns-output.csv', params[:index].to_i,  true )

    render :json => @data.to_json

  end


  def delete_xp
    xp = params[:id]

    unless xp.nil? || !Dir.exist?("#{Rails.root}/public/results/#{ xp }")
      path = Rails.root.join('public', 'results', xp)
      FileUtils.rm_r path
    end

    redirect_to root_path
  end


  def get_data


    render :json => {data: {:nodes => nodes, :links => links}, patterns: {}}
  end
  def show
    graph_io = params[:graph_path]
    pattern_io = params[:pattern_path]
    File.open(Rails.root.join('public', 'uploads', graph_io.original_filename), 'wb'){ |file| file.write(graph_io.read) }
    File.open(Rails.root.join('public', 'uploads', pattern_io.original_filename), 'wb'){ |file| file.write(pattern_io.read) }


    @view = {
        :graph => Rails.root.join('public', 'uploads', graph_io.original_filename),
        :pattern => Rails.root.join('public', 'uploads', pattern_io.original_filename)
    }
  end

  private



  def ensure_params
    redirect_to root_path unless params[:graph_path].present? && params[:pattern_path].present?
  end


end