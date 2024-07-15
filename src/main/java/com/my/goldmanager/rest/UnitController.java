package com.my.goldmanager.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.service.UnitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/units")
public class UnitController {

	@Autowired
	private UnitService unitService;

	@GetMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public List<Unit> getAll() {
		return unitService.listAll();
	}

	@GetMapping(path = "/{name}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Unit> getByName(@PathVariable(name = "name") String name) {
		Optional<Unit> optional = unitService.getByName(name);
		if (optional.isPresent()) {
			return ResponseEntity.ok(optional.get());
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Unit> create(@RequestBody Unit unit) {
		return ResponseEntity.status(HttpStatus.CREATED).body(unitService.save(unit));
	}

	@PutMapping(path = "/{name}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Unit> update(@PathVariable(name = "name") String name, @RequestBody Unit unit) {

		Optional<Unit> optionalUnit = unitService.update(name, unit);
		if (optionalUnit.isPresent()) {
			return ResponseEntity.ok(optionalUnit.get());
		}
		return ResponseEntity.notFound().build();
	}
	

	@DeleteMapping(path = "/{name}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> delete(@PathVariable(name = "name") String name) {
		if (unitService.deleteByName(name)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
