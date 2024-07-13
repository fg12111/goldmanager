package com.my.goldmanager.rest;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.my.goldmanager.rest.entity.PriceHistoryList;
import com.my.goldmanager.service.PriceHistoryService;

@RestController
@RequestMapping("/priceHistory")
public class PriceHistoryController {

	@Autowired
	private PriceHistoryService priceHistoryService;

	@GetMapping(path = "/{materialId}")
	public ResponseEntity<PriceHistoryList> listAllforMaterial(@PathVariable("materialId") String materialId) {
		Optional<PriceHistoryList> result = priceHistoryService.listAllForMaterial(materialId);
		if (result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}

		return ResponseEntity.notFound().build();

	}
}
