
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

import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.service.ItemTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController()
@RequestMapping("/itemTypes")
public class ItemTypeController {

	@Autowired
	private ItemTypeService itemTypeService;

	/**
	 * Create {@link ItemType}
	 * @param itemType
	 * @return
	 */
	@PostMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<ItemType> create(@RequestBody ItemType itemType) {
		ItemType savedItemType =itemTypeService.create(itemType); 
		return ResponseEntity.status(HttpStatus.CREATED).body(savedItemType);
		
	}
	
	/**
	 * Updates provided {@link ItemType}
	 * @param id
	 * @param itemType
	 * @return
	 */
	@PutMapping(path = "/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<ItemType> update(@PathVariable(name = "id") String id, @RequestBody ItemType itemType){
		Optional<ItemType> result = itemTypeService.update(id, itemType);
		if(result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * Returns ItemType by Id
	 * @param id
	 * @return
	 */
	@GetMapping(path ="/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<ItemType> get(@PathVariable(name = "id") String id){
		Optional<ItemType> result = itemTypeService.getById(id);
		if(result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();
	}
	
	/**
	 * Delete ItemType by id
	 * @param id
	 * @return
	 */
	@DeleteMapping(path="/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> delete(@PathVariable(name = "id") String id){
		if(itemTypeService.delete(id)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}
	
	/**
	 * List all Item types
	 * @return
	 */
	@GetMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public List<ItemType> list() {
		return itemTypeService.list();
	}

}
