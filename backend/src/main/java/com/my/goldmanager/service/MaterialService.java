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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class MaterialService {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
			.withZone(ZoneId.of("UTC"));

	@Autowired
	private MaterialRepository materialRepository;
	@Autowired
	private MaterialHistoryRepository materialHistoryRepository;

	public Material store(Material material) throws ValidationException {
		material.setId(null);
		if (material.getName() == null || material.getName().isBlank()) {
			throw new ValidationException("Material name is mandatory. ");
		}
		if (material.getEntryDate() == null) {
			material.setEntryDate(new Date());
		}

		material = materialRepository.save(material);
		saveMaterialHistory(material);
		return material;
	}

	public Optional<Material> update(String id, Material material) throws ValidationException {
		Optional<Material> oldOptional = materialRepository.findById(id);
		if (oldOptional.isPresent()) {

			Material old = oldOptional.get();
			if (material.getEntryDate() != null && (material.getEntryDate().before(old.getEntryDate())
					|| material.getEntryDate().equals(old.getEntryDate()))) {
				throw new ValidationException("EntryDate must be after " + formatDateToUTC(old.getEntryDate()));
			}
			if (material.getEntryDate() == null) {
				material.setEntryDate(new Date());
			}
			material = materialRepository.save(material);
			saveMaterialHistory(material);
			return Optional.of(material);
		}
		return Optional.empty();
	}

	private void saveMaterialHistory(Material material) {
		MaterialHistory mh = new MaterialHistory();
		mh.setPrice(material.getPrice());
		mh.setEntryDate(material.getEntryDate());
		mh.setMaterial(material);
		materialHistoryRepository.save(mh);

	}

	public List<Material> list() {
		return materialRepository.findAll();
	}

	public Optional<Material> getById(String id) {
		return materialRepository.findById(id);
	}

	public boolean deleteById(String id) {
		if (materialRepository.existsById(id)) {
			materialRepository.deleteById(id);
			return true;
		}
		return false;
	}

	private static String formatDateToUTC(Date date) {
		return dtf.format(date.toInstant()) + "+00:00";
	}
}
