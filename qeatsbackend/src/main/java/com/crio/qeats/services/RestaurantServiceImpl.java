
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
        LocalTime _8 =  LocalTime.of(7, 59, 59);
        LocalTime _10 =  LocalTime.of(10, 0, 1);
        LocalTime _1 =  LocalTime.of(12, 59, 59);
        LocalTime _2 =  LocalTime.of(14, 0, 1);
        LocalTime _7 =  LocalTime.of(18, 59, 59);
        LocalTime _9 =  LocalTime.of(21, 0, 1);
        Double latitude = getRestaurantsRequest.getLatitude();
        Double longitude = getRestaurantsRequest.getLongitude();
        List<Restaurant> restaurant = new ArrayList<>();
        System.out.println(currentTime);
        if (
          ( currentTime.isAfter( _8 ) && currentTime.isBefore( _10 ) )
          ||
          ( currentTime.isAfter( _1 ) && currentTime.isBefore( _2 ) )
          ||
          ( currentTime.isAfter( _7 ) && currentTime.isBefore( _9 ) )
          ){
            restaurant = restaurantRepositoryService.findAllRestaurantsCloseBy(
              latitude, longitude, currentTime, peakHoursServingRadiusInKms
              );
        }
        else{
          restaurant = restaurantRepositoryService.findAllRestaurantsCloseBy(latitude, 
          longitude, currentTime, normalHoursServingRadiusInKms);
        }
        System.out.println(restaurant);
        System.out.println(currentTime);
        GetRestaurantsResponse restaurants = new GetRestaurantsResponse(restaurant);
        return restaurants;
  }
}

