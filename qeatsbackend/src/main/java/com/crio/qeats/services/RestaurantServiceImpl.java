
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
    LocalTime t8 =  LocalTime.of(7, 59, 59);
    LocalTime t10 =  LocalTime.of(10, 0, 1);
    LocalTime t1 =  LocalTime.of(12, 59, 59);
    LocalTime t2 =  LocalTime.of(14, 0, 1);
    LocalTime t7 =  LocalTime.of(18, 59, 59);
    LocalTime t9 =  LocalTime.of(21, 0, 1);
    Double latitude = getRestaurantsRequest.getLatitude();
    Double longitude = getRestaurantsRequest.getLongitude();
    GetRestaurantsResponse restaurants;
    if (
        (currentTime.isAfter(t8) && currentTime.isBefore(t10))
        ||
        (currentTime.isAfter(t1) && currentTime.isBefore(t2))
        ||
        (currentTime.isAfter(t7) && currentTime.isBefore(t9))
    ) {
      restaurants = new GetRestaurantsResponse(
      restaurantRepositoryService.findAllRestaurantsCloseBy(
      latitude, longitude, currentTime, peakHoursServingRadiusInKms
      )
        );
    } else {
      restaurants = new GetRestaurantsResponse(
      restaurantRepositoryService.findAllRestaurantsCloseBy(latitude, 
      longitude, currentTime, normalHoursServingRadiusInKms)
      );
    }
    return restaurants;
  }
}

