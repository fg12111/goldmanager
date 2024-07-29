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
package com.my.goldmanager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.repository.ItemTypeRepository;

@Service
public class ItemTypeService {

	@Autowired
	private ItemTypeRepository repository;

	public ItemType create(ItemType itemType) {
		return repository.save(itemType);
	}

	public Optional<ItemType> update(String id, ItemType itemType) {
		if (repository.existsById(id)) {
			itemType.setId(id);
			return Optional.of(repository.save(itemType));
		}
		return Optional.empty();
	}

	public Optional<ItemType> getById(String id) {
		return repository.findById(id);
	}

	public List<ItemType> list() {
		return repository.findAll();
	}

	public boolean delete(String id) {
		if (repository.existsById(id)) {
			repository.deleteById(id);
			return true;
		}
		return false;
	}
}
