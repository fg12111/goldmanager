/** Copyright 2024 fg12111

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 * 
 */
package com.my.goldmanager.rest;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.my.goldmanager.rest.entity.Price;
import com.my.goldmanager.rest.entity.PriceGroupMap;
import com.my.goldmanager.rest.entity.PriceList;
import com.my.goldmanager.service.PriceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/prices")
public class PriceController {

	@Autowired
	private PriceService priceService;

	@GetMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PriceList> listPrices() {

		PriceList priceList = priceService.listAll();
		return ResponseEntity.ok(priceList);
	}

	@GetMapping(path = "/item/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Price> getPriceForItem(@PathVariable("id") String itemId) {
		Optional<Price> result = priceService.getPriceofItem(itemId);
		if (result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping(path = "/material/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PriceList> listPricesForMaterial(@PathVariable("id") String materialId) {
		Optional<PriceList> result = priceService.listForMaterial(materialId);
		if (result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping(path = "/groupBy/material")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PriceGroupMap> groupByMaterial() {

		return ResponseEntity.ok(priceService.groupByMaterial());
	}

	@GetMapping(path = "/groupBy/itemType")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PriceGroupMap> groupByItemtype() {

		return ResponseEntity.ok(priceService.groupByItemType());
	}
}
