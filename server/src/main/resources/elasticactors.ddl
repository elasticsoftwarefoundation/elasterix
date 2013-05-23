set ActorSystems['Http']['nrOfShards'] = int(8);
set ActorSystems['Http']['configurationClass'] = 'org.elasticsoftware.elasticactors.http.HttpActorSystem';
set ActorSystems['ElasterixServer']['nrOfShards'] = int(8);
set ActorSystems['ElasterixServer']['configurationClass'] = 'org.elasticsoftware.elasterix.server.ElasterixServer';