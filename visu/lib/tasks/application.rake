namespace :application do

  task :seed => :environment do

    require 'json/stream'

    $redis = Redis.new(:host => 'localhost', :port => 6379)

    json = JSON::Stream::Parser.parse((File.read( Rails.root.join('public', 'iris_wgs84.json') )))
    $redis = Redis.new(:host => 'localhost', :port => 6379);json.each { |iris| $redis.set(iris['properties']['DCOMIRIS'], iris)}
  end
end