package io.github.icodegarden.beecomb.master.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.master.mapper.UserMapper;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.Update;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.CreateUserApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.UpdatePasswordApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.UpdatePasswordNonOldApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.UpdateUserApiDTO;
import io.github.icodegarden.beecomb.master.security.UserUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.exception.SQLConstraintException;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class UserManager {

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserMapper userMapper;

	/**
	 * 
	 * @param dto
	 * @return
	 * @throws 参数错误、唯一约束
	 */
	public UserPO create(CreateUserApiDTO dto) throws IllegalArgumentException {
		UserPO po = new UserPO();
		BeanUtils.copyProperties(dto, po);

		po.setActived(true);
		po.setPassword(passwordEncoder.encode(dto.getPassword()));
		po.setPlatformRole(dto.getPlatformRole());
		po.setCreatedAt(SystemUtils.now());
		po.setCreatedBy(SecurityUtils.getUsername());
		po.setUpdatedAt(SystemUtils.now());
		po.setUpdatedBy(SecurityUtils.getUsername());
		try {
			userMapper.add(po);
			return po;
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}
	}

	public Page<UserPO> page(UserQuery query) {
		PageHelper.startPage(query.getPage(), query.getSize());

		Page<UserPO> page = (Page<UserPO>) userMapper.findAll(query);
		return page;
	}

	public UserPO findOne(Long id, UserQuery.With with) {
		return userMapper.findOne(id, with);
	}

	public UserPO findByUsername(String username, UserQuery.With with) {
		return userMapper.findByUsername(username, with);
	}

	/**
	 * 
	 * @param dto
	 * @return
	 * @throws 参数错误、唯一约束
	 */
	public void update(UpdateUserApiDTO dto) throws IllegalArgumentException {
		Update update = new UserPO.Update(dto.getId());
		BeanUtils.copyProperties(dto, update);

		doUpdate(update);
	}

	public void updatePassword(UpdatePasswordNonOldApiDTO dto) throws IllegalArgumentException {
		UserPO user = findOne(dto.getId(), UserQuery.With.WITH_LEAST);
		Assert.notNull(user, "user not found");

		Update update = new UserPO.Update(user.getId());
		update.setPassword(passwordEncoder.encode(dto.getPassword()));

		doUpdate(update);
	}

	public void updatePassword(UpdatePasswordApiDTO dto) throws IllegalArgumentException {
		UserPO user = findOne(dto.getId(), UserQuery.With.WITH_LEAST);
		Assert.notNull(user, "user not found");

		Long cUserId = UserUtils.getUserId();
		Assert.isTrue(user.getId().equals(cUserId), "Not Found, Ownership");
		Assert.isTrue(matchesPassword(dto.getPasswordOld(), user.getPassword()), "Original password not match");

		Update update = new UserPO.Update(user.getId());
		update.setPassword(passwordEncoder.encode(dto.getPasswordNew()));

		doUpdate(update);
	}

	public void enable(Long id) throws IllegalArgumentException {
		Update update = new UserPO.Update(id);
		update.setActived(true);

		doUpdate(update);
	}

	public void disable(Long id) throws IllegalArgumentException {
		Update update = new UserPO.Update(id);
		update.setActived(false);

		doUpdate(update);
	}

	private void doUpdate(Update update) {
		int i = 0;
		try {
			update.setUpdatedAt(SystemUtils.now());
			update.setUpdatedBy(SecurityUtils.getUsername());
			i = userMapper.update(update);
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}

		Assert.isTrue(i != 0, "user not found");
	}

	public boolean matchesPassword(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
}
