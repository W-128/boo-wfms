package cn.edu.sysu.workflow.access.dao;

import cn.edu.sysu.workflow.access.BooAccessApplication;
import cn.edu.sysu.workflow.common.entity.access.Account;
import cn.edu.sysu.workflow.common.util.IdUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link AccountDAO}
 *
 * @author Skye
 * Created on 2020/4/27
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BooAccessApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class AccountDAOTest {

    @Autowired
    private AccountDAO accountDAO;

    private Account account;

    @Before
    public void setUp() {
        this.account = new Account();
        this.account.setAccountId("test-account-" + IdUtil.nextId());
        this.account.setUsername("username");
        this.account.setPassword("password");
        this.account.setSalt("salt");
        this.account.setOrganizationName("organizationName");
        this.account.setStatus(1);
        this.account.setLevel(2);
    }

    /**
     * Test CRUD
     */
    @Test
    @Transactional
    public void test1() {
        // save
        Assert.assertEquals(1, accountDAO.save(account));
        // findOne

    }

    /**
     * Test {@link AccountDAO#findSimpleOne(String)}
     */
    @Test
    @Transactional
    public void test2() {
        // save
        Assert.assertEquals(1, accountDAO.save(account));

        // findSimpleOne
        Assert.assertEquals("username", accountDAO.findSimpleOne(account.getAccountId()).getUsername());
        Assert.assertNull(accountDAO.findSimpleOne(account.getAccountId()).getPassword());
    }

    /**
     * Test {@link AccountDAO#checkAccountByUsernameAndOrganizationName(String, String)}
     */
    @Test
    @Transactional
    public void test3() {
        // save
        Assert.assertEquals(1, accountDAO.save(account));

        // checkAccountByUsernameAndOrganizationName
        Assert.assertTrue(accountDAO.checkAccountByUsernameAndOrganizationName("username", "organizationName"));
    }

}
