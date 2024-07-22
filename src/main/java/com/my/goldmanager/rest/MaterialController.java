package com.my.goldmanager.rest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.my.goldmanager.entity.Material;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.MaterialService;
import com.my.goldmanager.service.exception.BadRequestException;
import com.my.goldmanager.service.exception.ValidationException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/materials")
public class MaterialController {
	private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);
	@Autowired
	private MaterialService materialService;

	@PostMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Material> create(@RequestBody Material material) {
		try {
			Material savedmaterial = materialService.store(material);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedmaterial);
		} catch (ValidationException ve) {
			throw new BadRequestException(ve.getMessage(), ve);
		}
	}

	@GetMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public List<Material> list() {
		return materialService.list();
	}

	@GetMapping(path = "/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Material> getbyId(@PathVariable(name = "id") String id) {
		Optional<Material> result = materialService.getById(id);
		if (result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();

	}

	@PutMapping(path = "/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Material> update(@PathVariable(name = "id") String id, @RequestBody Material material) {
		try {
			Optional<Material> result = materialService.update(id, material);
			if (result.isPresent()) {
				return ResponseEntity.ok(result.get());
			}
			return ResponseEntity.notFound().build();
		} catch (ValidationException ve) {
			logger.error("Validation error during updating Material", ve);
			throw new BadRequestException(ve.getMessage(), ve);
		}
	}

	@DeleteMapping(path = "/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> delete(@PathVariable(name = "id") String id) {
		if (materialService.deleteById(id)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
