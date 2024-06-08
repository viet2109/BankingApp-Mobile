package matcha.banking.be.dao;

import matcha.banking.be.entity.TransitionEntity;
import org.springframework.data.repository.CrudRepository;

public interface TransitionDao extends CrudRepository<TransitionEntity, Integer> {
}
