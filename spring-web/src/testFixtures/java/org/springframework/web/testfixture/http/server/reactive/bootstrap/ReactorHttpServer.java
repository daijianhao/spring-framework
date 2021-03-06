/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.testfixture.http.server.reactive.bootstrap;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import reactor.netty.DisposableServer;

import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * spring webflux的响应式 http server 的实现类
 * 这里利用 reactor.netty 提供的{@link reactor.netty.http.server.HttpServer}中的{@link HttpServer.HttpServerHandle}类
 * 传入一个自定义的 {@link BiFunction<? super  HttpServerRequest , ? super  HttpServerResponse , ? extends Publisher<Void>> handler}
 * 来接受http响应在各个阶段的变化即状态，这里即是：{@link ReactorHttpServer#reactorHandler}
 *
 * @author Stephane Maldini
 */
public class ReactorHttpServer extends AbstractHttpServer {

	private ReactorHttpHandlerAdapter reactorHandler;

	private reactor.netty.http.server.HttpServer reactorServer;

	private AtomicReference<DisposableServer> serverRef = new AtomicReference<>();


	@Override
	protected void initServer() {
		this.reactorHandler = createHttpHandlerAdapter();
		this.reactorServer = reactor.netty.http.server.HttpServer.create()
				.host(getHost()).port(getPort());
	}

	private ReactorHttpHandlerAdapter createHttpHandlerAdapter() {
		return new ReactorHttpHandlerAdapter(resolveHttpHandler());
	}

	@Override
	protected void startInternal() {
		DisposableServer server = this.reactorServer.handle(this.reactorHandler).bind().block();
		setPort(((InetSocketAddress) server.address()).getPort());
		this.serverRef.set(server);
	}

	@Override
	protected void stopInternal() {
		this.serverRef.get().dispose();
	}

	@Override
	protected void resetInternal() {
		this.reactorServer = null;
		this.reactorHandler = null;
		this.serverRef.set(null);
	}

}
