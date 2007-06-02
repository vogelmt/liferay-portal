<%
/**
 * Copyright (c) 2000-2007 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
%>

<%@ include file="/html/portlet/web_form/init.jsp" %>

<%
String title = prefs.getValue("title", StringPool.BLANK);
String description = prefs.getValue("description", StringPool.BLANK);
boolean requireCaptcha = GetterUtil.getBoolean(prefs.getValue("require-captcha", StringPool.BLANK));
%>

<form action="<portlet:actionURL><portlet:param name="struts_action" value="/web_form/view" /></portlet:actionURL>" method="post">

<h3><%= title %></h3>

<br />

<%= description %>

<br /><br />

<liferay-ui:success key="emailSent" message="the-email-was-sent-successfuly" />

<liferay-ui:error exception="<%= CaptchaTextException.class %>" message="text-verification-failed" />
<liferay-ui:error key="allFieldsRequired" message="please-complete-all-fields" />
<liferay-ui:error key="emailNotSent" message="the-email-could-not-be-sent" />

<%
for (int i = 1; i <= 10; i++) {
    String fieldName = "field" + i;
    String fieldLabel = prefs.getValue("fieldLabel" + i, "");
	String fieldValue = ParamUtil.getString(request, fieldName);

	if (Validator.isNull(fieldLabel)) {
       continue;
    }

	String type = prefs.getValue("fieldType" + i, "text");
%>

	<label for="<portlet:namespace /><%= fieldName %>"><%= fieldLabel %></label><br />

	<c:choose>
		<c:when test='<%= type.equals("text") %>'>
			<input class="liferay-input-text" name="<portlet:namespace /><%= fieldName %>" type="text" value="<%= fieldValue %>" />
		</c:when>
		<c:when test='<%= type.equals("textarea") %>'>
			<textarea class="liferay-textarea" name="<portlet:namespace /><%= fieldName %>" wrap="soft"><%= fieldValue %></textarea>
		</c:when>
		<c:when test='<%= type.equals("options") %>'>

			<%
			String[] options = WebFormUtil.split(prefs.getValue("fieldOptions" + i, "unknown"));
			%>

			<select name="<portlet:namespace /><%= fieldName %>">

			<%
			for (int j = 0; j < options.length; j++) {
			%>

				<option <%= fieldValue.equals(options[j]) ? "selected" : "" %> value="<%= options[j] %>"><%= options[j] %></option>

			<%
			}
			%>

			</select>
		</c:when>
	</c:choose>

	<br /><br />

<%
}
%>

<c:if test="<%= requireCaptcha %>">
	<portlet:actionURL windowState="<%= LiferayWindowState.EXCLUSIVE.toString() %>" var="captchaURL">
		<portlet:param name="struts_action" value="/web_form/captcha" />
	</portlet:actionURL>

	<liferay-ui:captcha url="<%= captchaURL %>" />
</c:if>

<input type="submit" value="<liferay-ui:message key="send" />" />

</form>