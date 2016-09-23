class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception


  helper_method :type_description



  def type_description( _type )
    case _type
      when 'application'
        return {description:'Nodes', icon:'sitemap'}
      when 'map'
        return {description:'Google Maps', icon:'map'}
      when 'game'
        return {description:'Dota 2 game', icon:'game'}
    end
  end
end
