package com.swissas;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.swissas.util.SwissAsStorage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Allows the unit tests to use the code calling {@link SwissAsStorage#getInstance()} without starting an IDE:
 * the application is replaced by a mock that only knows the storage.
 *
 * @author Tavan Alain
 */
public final class TestApplication implements AutoCloseable {
	private final Disposable disposable = Disposer.newDisposable("swissas test application");
	private final SwissAsStorage storage = new SwissAsStorage();
	
	private TestApplication() {
		Application application = mock(Application.class);
		when(application.getService(SwissAsStorage.class)).thenReturn(this.storage);
		ApplicationManager.setApplication(application, this.disposable);
	}
	
	/**
	 * Installs the mocked application, it is removed when the returned object is closed.
	 */
	public static TestApplication install() {
		return new TestApplication();
	}
	
	public SwissAsStorage getStorage() {
		return this.storage;
	}
	
	@Override
	public void close() {
		Disposer.dispose(this.disposable);
	}
}
