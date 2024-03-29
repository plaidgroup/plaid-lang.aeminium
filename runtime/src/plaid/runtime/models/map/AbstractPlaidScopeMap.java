package plaid.runtime.models.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import plaid.runtime.PlaidObject;
import plaid.runtime.PlaidScope;

/**
 * Abstract class for the implementation of some common functionality of both 
 * local and global scopes.
 * 
 * @author mhahnenberg
 *
 */
public abstract class AbstractPlaidScopeMap implements PlaidScope {
	protected final Map<String, PlaidObject> immutableScopeMap;
	protected final Map<String, PlaidObject> mutableScopeMap;
	
	public AbstractPlaidScopeMap() {
		this.immutableScopeMap = new ConcurrentHashMap<String, PlaidObject>();
		this.mutableScopeMap = new ConcurrentHashMap<String, PlaidObject>();
	}

	@Override
	public void insert(String name, PlaidObject plaidObj) {
		this.insert(name, plaidObj, true);
	}
}
