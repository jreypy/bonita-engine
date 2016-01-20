/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.tenant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import com.bonitasoft.engine.APITestSPUtil;
import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.api.IdentityAPI;
import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.TenantStatusException;
import com.bonitasoft.engine.bpm.flownode.ArchivedProcessInstancesSearchDescriptor;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor;
import com.bonitasoft.engine.platform.TenantActivationException;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantDeactivationException;
import com.bonitasoft.engine.platform.TenantNotFoundException;
import com.bonitasoft.engine.test.junit.BonitaEngineSPRule;
import org.bonitasoft.engine.PrintTestsStatusRule;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;
import org.bonitasoft.engine.theme.ThemeType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.APIClient;
import com.bonitasoft.engine.api.IdentityAPI;
/**
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class TenantIT {

    @Rule
    public BonitaEngineRule bonitaEngineSPRule = BonitaEngineSPRule.create();

    private final static String userName = "tenant_name";

    private final static String password = "tenant_password";

    private static long tenantId;

    private static final Object LOCK = new Object();

    private static PlatformAPI platformAPI;

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession session;

    private APIClient apiClient = new APIClient();

    private final Logger LOGGER = LoggerFactory.getLogger(TenantIT.class);

    @Rule
    public TestRule testWatcher = new PrintTestsStatusRule(LOGGER) {

        @Override
        public List<String> clean() throws Exception {
            return Collections.emptyList();
        }
    };

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        logAsPlatformAdmin();
        createTenant();
    }

    private static void createTenant() throws CreationException, TenantNotFoundException, TenantActivationException {
        tenantId = platformAPI.createTenant(new TenantCreator("tenant", "tenant", "testIconName", "testIconPath", userName, password));
        platformAPI.activateTenant(tenantId);
        tenantId = platformAPI.getTenantByName("tenant").getId();
    }

    private static void logAsPlatformAdmin() throws PlatformLoginException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        session = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        deleteTenant();
        platformLoginAPI.logout(session);
    }

    private static void deleteTenant() throws TenantNotFoundException, TenantDeactivationException, DeletionException {
        platformAPI.deactiveTenant(tenantId);
        platformAPI.deleteTenant(tenantId);
    }

    @Test
    public void singleThreadTenant() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login(tenantId, userName, password);

        final IdentityAPI identityAPI = apiClient.getIdentityAPI();
        identityAPI.createUser("auser1", "bpm");
        final List<User> users = identityAPI.getUsers(0, 5, UserCriterion.USER_NAME_ASC);
        assertEquals(1, users.size());

        identityAPI.deleteUser("auser1");
        apiClient.logout();
    }

    @Test
    public void deactivateTenantDeleteSession() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login(tenantId, userName, password);

        final IdentityAPI identityAPI = apiClient.getIdentityAPI();
        // will work
        identityAPI.getNumberOfUsers();
        platformAPI.deactiveTenant(tenantId);
        try {
            identityAPI.getNumberOfUsers();
            fail("should not be able to call an api on a deactivated tenant");
        } catch (final InvalidSessionException e) {
            // Oracle deadlock can occur here, but does not appear to have functional issue:
            platformAPI.activateTenant(tenantId);
            apiClient.logout();
        }

    }

    @Test(expected = TenantStatusException.class)
    @Cover(classes = { ServerAPI.class }, jira = "BS-2242", keywords = { "TenantIsPausedException, tenant paused" }, concept = BPMNConcept.NONE)
    public void cannotAccessTenantAPIsOnPausedTenant() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login(tenantId, userName, password);

        final TenantAdministrationAPI tenantManagementAPI = apiClient.getTenantAdministrationAPI();
        tenantManagementAPI.pause();
        // Here session has been cleaned, so need to login again:
        apiClient.login(tenantId, userName, password);
        try {
            apiClient.getProcessAPI().getNumberOfProcessInstances();
        } finally {
            tenantManagementAPI.resume();
            apiClient.logout();
        }
    }

    @Test
    @Cover(classes = { ServerAPI.class }, jira = "BS-7101", keywords = { "tenant pause" }, concept = BPMNConcept.NONE)
    public void should_be_able_to_login_only_with_technical_user_on_paused_tenant() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login(tenantId, userName, password);
        final IdentityAPI identityAPI = apiClient.getIdentityAPI();
        final User john = identityAPI.createUser("john", "bpm");
        final TenantAdministrationAPI tenantManagementAPI = apiClient.getTenantAdministrationAPI();
        tenantManagementAPI.pause();
        apiClient.logout();
        // login with normal user: not working
        try {
            apiClient.login(tenantId, "john", "bpm");
            fail("Should not be able to login using other user than technical");
        } catch (final TenantStatusException e) {
            // ok, can't login with user that is not technical
        }
        // login with normal user: not working
        apiClient.login(tenantId, userName, password);
        // ok to login with technical user
        apiClient.getTenantAdministrationAPI().resume();
        apiClient.logout();
        // can now login with normal user
        apiClient.login(tenantId, "john", "bpm");
        apiClient.logout();

        // delete the user
        apiClient.login(tenantId, userName, password);
        apiClient.getIdentityAPI().deleteUser(john.getId());
        apiClient.logout();
    }

    @Test
    @Cover(classes = { ServerAPI.class }, jira = "BS-2242", keywords = { "tenant pause" }, concept = BPMNConcept.NONE)
    public void pauseAnnotatedAPIMethodShouldBePossibleOnPausedTenant() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login(tenantId, userName, password);
        apiClient.getThemeAPI().setCustomTheme("zipFile".getBytes(), "cssContent".getBytes(), ThemeType.PORTAL);
        final TenantManagementAPI tenantManagementAPI = TenantAPIAccessor.getTenantManagementAPI(apiClient.getSession());
        tenantManagementAPI.pause();
        try {
            tenantManagementAPI.isPaused();
            // test with sp accessor
            apiClient.getThemeAPI().getLastUpdateDate(ThemeType.PORTAL);
            apiClient.getIdentityAPI().getNumberOfUsers();
            apiClient.getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 1).done());
            apiClient.getCustomPageAPI().searchPages(new SearchOptionsBuilder(0, 1).done());
            // test with bos accessor
            org.bonitasoft.engine.api.TenantAPIAccessor.getThemeAPI(apiClient.getSession()).getLastUpdateDate(ThemeType.PORTAL);
            org.bonitasoft.engine.api.TenantAPIAccessor.getIdentityAPI(apiClient.getSession()).getNumberOfUsers();
            org.bonitasoft.engine.api.TenantAPIAccessor.getProfileAPI(apiClient.getSession()).searchProfiles(new SearchOptionsBuilder(0, 1).done());
        } finally {
            tenantManagementAPI.resume();
            apiClient.logout();
        }
    }

    @Test
    public void timerNotDeletedWhenTenantIsDeactivated() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login(tenantId, userName, password);
        ProcessAPI processAPI = apiClient.getProcessAPI();
        final ProcessDefinitionBuilderExt processDefinitionBuilderExt = new ProcessDefinitionBuilderExt();
        processDefinitionBuilderExt.createNewInstance("aProcess", "1.0");

        processDefinitionBuilderExt.addStartEvent("start").addTimerEventTriggerDefinition(TimerType.CYCLE,
                new ExpressionBuilder().createConstantStringExpression("* * * * * ?"));
        processDefinitionBuilderExt.addAutomaticTask("auto").addShortTextData("data", new ExpressionBuilder().createGroovyScriptExpression("script",
                "System.out.println(\"Process started from timer\");return \"test\";", String.class.getName()));
        processDefinitionBuilderExt.addTransition("start", "auto");
        final ProcessDefinition process = processAPI.deploy(new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(processDefinitionBuilderExt.done()).done());
        processAPI.enableProcess(process.getId());
        apiClient.logout();
        logAsPlatformAdmin();
        platformAPI.deactiveTenant(tenantId); // Oracle deadlock can occur here, but does not appear to have functional issue
        platformAPI.activateTenant(tenantId);
        apiClient.login(tenantId, userName, password);
        processAPI = apiClient.getProcessAPI();
        final ProcessAPI fprocessAPI = processAPI;
        // the timer should have created at least 1 instance after the activate
        new WaitUntil(30, 20000) {

            @Override
            protected boolean check() throws Exception {
                final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
                searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, process.getId());
                final SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances = fprocessAPI.searchArchivedProcessInstances(searchOptionsBuilder
                        .done());
                return searchArchivedProcessInstances.getCount() > 1;
            }
        }.waitUntil();
        waitForInstancesToFinishAndRemoveProcess(process, fprocessAPI);
        apiClient.logout();
    }

    protected void waitForInstancesToFinishAndRemoveProcess(final ProcessDefinition process, final ProcessAPI processAPI) throws Exception {
        // Disable the process, so that no new instances are started:
        processAPI.disableProcess(process.getId());

        // Wait for running instances to finish:
        new WaitUntil(30, 20000) {

            @Override
            protected boolean check() throws Exception {
                final SearchResult<ProcessInstance> processInstances = processAPI
                        .searchProcessInstances(new SearchOptionsBuilder(0, 1).filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, process.getId())
                                .done());
                return processInstances.getCount() == 0;
            }
        }.waitUntil();

        long nbDeletedArchivedProcessInstances;
        do {
            nbDeletedArchivedProcessInstances = processAPI.deleteArchivedProcessInstances(process.getId(), 0, 100);
        } while (nbDeletedArchivedProcessInstances > 0);
        processAPI.deleteProcessDefinition(process.getId());
    }

    @Test
    public void multiThreadTenant() throws Exception {
        final LoginThread login = new LoginThread();
        final Thread loginThread = new Thread(login);
        final GetUserRequestThread getUser = new GetUserRequestThread(login);
        final Thread getUserThread = new Thread(getUser);
        loginThread.start();
        getUserThread.start();
        synchronized (LOCK) {
            while (!getUser.isDone() && !getUser.isFailed()) {
                try {
                    LOCK.wait();
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (getUser.isFailed()) {
            throw new Exception("failed to retrieve user");
        }
        final List<User> users = getUser.getUsers();
        assertNotNull(users);
        assertEquals(1, users.size());
        final IdentityAPI identityAPI = apiClient.getIdentityAPI();
        identityAPI.deleteUser("auser1");
        apiClient.logout();
    }

    class LoginThread implements Runnable {

        private boolean failed = false;

        @Override
        public void run() {
            synchronized (LOCK) {
                try {
                    apiClient.login(tenantId, userName, password);
                } catch (final Exception e) {
                    failed = true;
                    throw new RuntimeException(e);
                } finally {
                    LOCK.notifyAll();
                }
            }
        }

        public boolean isFailed() {
            return failed;
        }

    }

    class GetUserRequestThread implements Runnable {

        private boolean done = false;

        private List<User> users;

        private final LoginThread loginThread;

        private boolean failed;

        public GetUserRequestThread(final LoginThread login) {
            loginThread = login;
        }

        @Override
        public void run() {
            IdentityAPI identityAPI;
            synchronized (LOCK) {
                try {
                    while (apiClient.getSession() == null && !loginThread.isFailed()) {
                        LOCK.wait();
                    }
                    if (loginThread.isFailed()) {
                        failed = true;
                        throw new RuntimeException("login failed");
                    }
                    identityAPI = apiClient.getIdentityAPI();
                    identityAPI.createUser("auser1", "bpm");
                    users = identityAPI.getUsers(0, 5, UserCriterion.USER_NAME_ASC);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done = true;
                    LOCK.notifyAll();
                }
            }

        }

        public boolean isDone() {
            return done;
        }

        public List<User> getUsers() {
            return users;
        }

        public boolean isFailed() {
            return failed;
        }
    }

}