package nextstep.subway.auth.ui;

import nextstep.subway.auth.application.AuthService;
import nextstep.subway.auth.domain.AuthenticationPrincipal;
import nextstep.subway.auth.domain.GuestMember;
import nextstep.subway.auth.infrastructure.AuthorizationExtractor;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
	private AuthService authService;

	public AuthenticationPrincipalArgumentResolver(AuthService authService) {
		this.authService = authService;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		String accessToken = AuthorizationExtractor.extract(webRequest.getNativeRequest(HttpServletRequest.class));

		if (isTokenEmtpy(accessToken) && isAuthNotNecessary(parameter)) {
			return new GuestMember();
		}
		return authService.findMemberByToken(accessToken);
	}

	private boolean isTokenEmtpy(String accessToken) {
		return accessToken == null;
	}

	private boolean isAuthNotNecessary(MethodParameter parameter) {
		AuthenticationPrincipal authenticationPrincipal = parameter.getParameterAnnotation(
			AuthenticationPrincipal.class);
		return authenticationPrincipal != null && !authenticationPrincipal.required();
	}
}