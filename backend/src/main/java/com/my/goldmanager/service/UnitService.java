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

import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.repository.UnitRepository;
import com.my.goldmanager.service.exception.ValidationException;



@Service
public class UnitService {
	@Autowired
	private UnitRepository unitRepository;

	public Unit save(Unit unit) throws ValidationException {
		if(unit.getName() == null || unit.getName().isBlank()) {
			throw new ValidationException("Unit name is mandatory.");
		}
		return unitRepository.save(unit);
	}

	public Optional<Unit> update(String name, Unit unit) throws ValidationException {
		if (unitRepository.existsById(name)) {
			unit.setName(name);
			return Optional.of(save(unit));
		}
		return Optional.empty();
	}

	public boolean deleteByName(String name) {
		if (unitRepository.existsById(name)) {
			unitRepository.deleteById(name);
			return true;
		}
		return false;
	}

	public List<Unit> listAll() {
		return unitRepository.findAll();
	}

	public Optional<Unit> getByName(String name) {
		return unitRepository.findById(name);
	}
}
