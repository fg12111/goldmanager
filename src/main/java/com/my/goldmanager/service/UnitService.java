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
