package com.tdcr.docker.backend.crud;

import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.app.security.CurrentUser;
import com.tdcr.docker.backend.data.entity.AbstractEntity;
import com.tdcr.docker.backend.exceptions.UserFriendlyDataException;
import com.tdcr.docker.backend.service.CrudService;
import com.tdcr.docker.ui.views.EntityView;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;


@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityPresenter<T extends AbstractEntity, V extends EntityView<T>>
	implements HasLogger {

	public static final String ENTITY_NOT_FOUND = "The selected entity was not found.";

	public static final String CONCURRENT_UPDATE = "Somebody else might have updated the data. Please refresh and try again.";

	public static final String OPERATION_PREVENTED_BY_REFERENCES = "The operation can not be executed as there are references to entity in the database.";

	public static final String REQUIRED_FIELDS_MISSING = "Please fill out all required fields before proceeding.";


	private CrudService<T> crudService;

	private CurrentUser currentUser;

	private V view;

	public EntityPresenter(
		CrudService<T> crudService, CurrentUser currentUser) {
		this.crudService = crudService;
		this.currentUser = currentUser;
	}

	public void setView(V view) {
		this.view = view;
	}

	public V getView() {
		return view;
	}



	public boolean loadEntity(Long id, CrudOperationListener<T> onSuccess) {
		return executeOperation(() -> {
			onSuccess.execute(crudService.load(id));
		});
	}
	private boolean executeOperation(Runnable operation) {
		try {
			operation.run();
			return true;
		}
		catch (UserFriendlyDataException e) {
			// Commit failed because of application-level data constraints
			consumeError(e, e.getMessage(), true);
		}
		catch (DataIntegrityViolationException e) {
			// Commit failed because of validation errors
			consumeError(
					e, OPERATION_PREVENTED_BY_REFERENCES, true);
		}
		catch (OptimisticLockingFailureException e) {
			consumeError(e, CONCURRENT_UPDATE, true);
		}
		catch (EntityNotFoundException e) {
			consumeError(e, ENTITY_NOT_FOUND, false);
		}
		catch (ConstraintViolationException e) {
			consumeError(e, REQUIRED_FIELDS_MISSING, false);
		}
		return false;
	}
	private void consumeError(
			Exception e, String message, boolean isPersistent) {
		getLogger().debug(message, e);
		view.showError(message, isPersistent);
	}

	@FunctionalInterface
	public interface CrudOperationListener<T> {

		void execute(T entity);
	}

}
