package ca.bc.gov.educ.api.ruleengine.config;

import ca.bc.gov.educ.api.ruleengine.util.LogHelper;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;

@Component
public class RequestInterceptor implements HandlerInterceptor {

	@Autowired
	RuleEngineApiConstants constants;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// for async this is called twice so need a check to avoid setting twice.
		if (request.getAttribute("startTime") == null) {
			final long startTime = Instant.now().toEpochMilli();
			request.setAttribute("startTime", startTime);
		}
		return true;
	}

	/**
	 * After completion.
	 *
	 * @param request  the request
	 * @param response the response
	 * @param handler  the handler
	 * @param ex       the ex
	 */
	@Override
	public void afterCompletion(@NonNull final HttpServletRequest request, final HttpServletResponse response, @NonNull final Object handler, final Exception ex) {
		LogHelper.logServerHttpReqResponseDetails(request, response, constants.isSplunkLogHelperEnabled());
		val correlationID = request.getHeader(RuleEngineApiConstants.CORRELATION_ID);
		if (correlationID != null) {
			response.setHeader(RuleEngineApiConstants.CORRELATION_ID, request.getHeader(RuleEngineApiConstants.CORRELATION_ID));
		}
	}
}
