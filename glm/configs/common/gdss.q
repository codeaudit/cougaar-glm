# gdss query

%SQLNamedAssetCreator
Database=jdbc:oracle:thin:@eiger.alpine.bbn.com:1521:alp
Username=alp_plugin
Password=alp_plugin
query = select ac_type, tail_fleet, ac_type \
  from gdss_aircraft \
  where home_icao = :icao \
  and ac_type in (:actypes)

