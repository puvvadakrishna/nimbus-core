/**
 *  Copyright 2016-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.antheminc.oss.nimbus.domain.model.state.repo.ws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.domain.model.state.repo.AbstractWSModelRepository;
import com.antheminc.oss.nimbus.domain.model.state.repo.db.SearchCriteria;
import com.antheminc.oss.nimbus.domain.model.state.repo.db.SearchCriteria.ExampleSearchCriteria;
import com.antheminc.oss.nimbus.support.EnableLoggingInterceptor;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO: 
 * currently an open issue in spring-cloud-netflix: https://github.com/spring-cloud/spring-cloud-netflix/issues/1047
 * Once update is available, uncomment the feign client and remove direct restTemplate dependency.
 * 
 * In progress: Not all methods of ModelRepository are implemented yet, will be implemented as needed
 * 
 * @author Rakesh Patel
 */
@ConfigurationProperties(prefix="ext.repository")
@Getter @Setter
@EnableLoggingInterceptor
public class DefaultWSModelRepository extends AbstractWSModelRepository {

	private Map<String, String> targetUrl;
	
	//private ExternalModelRepositoryClient externalRepositoryClient;
	
	public DefaultWSModelRepository(BeanResolverStrategy beanResolver, RestTemplate restTemplate) {
		super(beanResolver, restTemplate);
		//this.externalRepositoryClient = beanResolver.get(ExternalModelRepositoryClient.class);
	}
	
	@Override
	public String getTargetUrl(String alias) {
		if(MapUtils.isNotEmpty(this.targetUrl))
			return StringUtils.isBlank(this.targetUrl.get(alias)) ? null : this.targetUrl.get(alias);
		
		return null;		
	}

	@Override
	public <T> T handleGet(Class<?> referredClass, URI uri) {
		ResponseEntity<T> responseEntity = (ResponseEntity<T>)getRestTemplate().exchange(new RequestEntity<T>(HttpMethod.GET, uri), referredClass);
		return Optional.ofNullable(responseEntity).map((response) -> response.getBody()).orElse(null);
	}

	@Override
	public <T> Object handleSearch(Class<?> referredDomainClass, Supplier<SearchCriteria<?>> criteriaSupplier,URI uri) {
		SearchCriteria<?> searchCriteria = criteriaSupplier.get();
		Object response = execute(() -> new RequestEntity<Object>(searchCriteria instanceof ExampleSearchCriteria ? searchCriteria.getWhere(): null, HttpMethod.POST, uri), 
					() -> new ParameterizedTypeReference<List<T>>() {
							public Type getType() {
								return new CustomParameterizedTypeImpl((ParameterizedType) super.getType(), new Type[] {referredDomainClass});
							}
					});
		return response;
	}

	@Override
	protected <T> T handleNew(Class<?> referredClass, URI uri) {
		throw new UnsupportedOperationException("_new operation is not supported for Database.rep_ws repository");
	}

	@Override
	protected <T> T handleUpdate(T state, URI uri) {
		throw new UnsupportedOperationException("_update operation is not supported for Database.rep_ws repository");
	}

	@Override
	protected <T> T handleDelete(URI uri) {
		throw new UnsupportedOperationException("_delete operation is not supported for Database.rep_ws repository");
	}


}
