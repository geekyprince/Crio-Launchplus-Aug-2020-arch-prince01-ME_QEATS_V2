/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import javax.swing.text.html.parser.Entity;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


@Service
@Primary
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.

  // TODO: CRIO_TASK_MODULE_REDIS
  // We want to use cache to speed things up. Write methods that perform the same functionality,
  // but using the cache if it is present and reachable.
  // Remember, you must ensure that if cache is not present, the queries are directed at the
  // database instead.
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
    //System.out.println(currentTime);
    //System.out.println("service");
    long startTimeInMillis = System.currentTimeMillis();
    List<RestaurantEntity> restaurantEntities = new ArrayList<>();
    
    try {
      //System.out.println("here i am ---->" + redisConfiguration.getJedisPool());
      redis.clients.jedis.Jedis jedis = redisConfiguration.getJedisPool().getResource();
      GeoHash geoHash = GeoHash.withCharacterPrecision(latitude, longitude, 7);
      
      String restsString = jedis.get(geoHash.toBase32());
      
      if (restsString != null && !restsString.equals("[]")) {
        //System.out.println("if find ------->");
        restaurantEntities =  new ObjectMapper().readValue(restsString, 
            new TypeReference<List<RestaurantEntity>>() {});
      } else {
        restaurantEntities = restaurantRepository.findAll();

        //System.out.println("kakakakakakakakakkakakakkrestaurantEntities : ");
        jedis.set(geoHash.toBase32(), new ObjectMapper().writeValueAsString(restaurantEntities));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    List<Restaurant> restaurants = new ArrayList<>();
    ModelMapper modelMapper = modelMapperProvider.get();
    
    //List<RestaurantEntity> restaurantEntities = mongoTemplate.findAll(RestaurantEntity.class);

    long endTimeInMillis = System.currentTimeMillis();

    System.out.println("Your function took :" + (endTimeInMillis - startTimeInMillis));

      //CHECKSTYLE:OFF
      //CHECKSTYLE:ON
    for (RestaurantEntity restent : restaurantEntities) {
      if (isRestaurantCloseByAndOpen(restent, currentTime, latitude, 
          longitude, servingRadiusInKms)) {
            
        restaurants.add(modelMapper.map(restent, Restaurant.class));
      }
    }
    //System.out.println("here------" + restaurants);
    return restaurants;
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?

  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }



}