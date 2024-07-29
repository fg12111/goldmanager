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

import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.MaterialHistoryRepository;

@Service
public class MaterialHistoryService {

	@Autowired
	private MaterialHistoryRepository repository;

	public List<MaterialHistory> getAll() {
		return repository.findAll();
	}
	
	public Optional<MaterialHistory> getbyId(String id){
		return repository.findById(id);
	}

	public List<MaterialHistory> findAllByMaterial(String materialId) {
		return repository.findByMaterial(materialId);
	}

	public boolean deleteById(String id) {
		if (repository.existsById(id)) {
			repository.deleteById(id);
			repository.flush();
			return true;
		}
		return false;
	}

	public void deleteAllByMaterial(String materialId) {
		repository.flush();
		repository.deleteAllInBatch(findAllByMaterial(materialId));
		repository.flush();
	}

}
