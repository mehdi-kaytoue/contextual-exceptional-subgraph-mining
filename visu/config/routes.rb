Rails.application.routes.draw do


  root 'welcome#welcome'

  get 'app', to: 'welcome#index'

  post 'show', to: 'welcome#show'
  get 'get_data', to: 'welcome#get_data'

  get '/patterns/:xp/:index', to: 'welcome#get_pattern'

  get '/delete/:id', to: 'welcome#delete_xp'
end
