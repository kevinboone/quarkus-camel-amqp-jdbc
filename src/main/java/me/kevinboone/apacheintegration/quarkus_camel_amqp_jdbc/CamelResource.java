/*===========================================================================
 
  CamelResource.java

  Copyright (c)2022 Kevin Boone, GPL v3.0

===========================================================================*/

package me.kevinboone.apacheintegration.quarkus_camel_amqp_jdbc;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.runtime.StartupEvent;
import java.sql.Connection;
import java.sql.Statement;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.apache.camel.CamelContext;

@ApplicationScoped
public class CamelResource 
  {
  @Inject
  @DataSource("camel-ds")
  AgroalDataSource dataSource;

  void startup (@Observes StartupEvent event, CamelContext context) 
     throws Exception 
    {
    context.getRouteController().startAllRoutes();
    }

  /** 
  Initialization code could go here, although it is currently commented
  out. This would be a reasonble place to initalise the database table,
  if required.
  */
  @PostConstruct
  void postConstruct() throws Exception 
    {
    /*
    try (Connection con = dataSource.getConnection()) 
      { 
      try (Statement statement = con.createStatement()) 
        {
        con.setAutoCommit(true); 
        try 
          {
          statement.execute("drop table camel"); 
          } catch (Exception ignored) {}
        statement.execute ("create table messages (body varchar(255));"); 
        } 
      }
    */
    }
  }

