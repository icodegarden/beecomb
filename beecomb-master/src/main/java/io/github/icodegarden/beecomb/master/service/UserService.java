package io.github.icodegarden.beecomb.master.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.common.db.exception.SQLIntegrityConstraintException;
import io.github.icodegarden.beecomb.master.mapper.UserMapper;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.Update;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateUserDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdatePasswordDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdatePasswordNonOldDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateUserDTO;
import io.github.icodegarden.beecomb.master.security.UserUtils;
import io.github.icodegarden.commons.lang.spec.response.ClientBizErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class UserService {

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
	public UserPO create(CreateUserDTO dto) throws ErrorCodeException {
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
			throw new ClientParameterInvalidErrorCodeException(
					ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(),
					new SQLIntegrityConstraintException(e).getMessage());
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
	public void update(UpdateUserDTO dto) throws ErrorCodeException {
		Update update = new UserPO.Update(dto.getId());
		BeanUtils.copyProperties(dto, update);

		doUpdate(update);
	}

	public void updatePassword(UpdatePasswordNonOldDTO dto) throws ErrorCodeException {
		UserPO user = findOne(dto.getId(), UserQuery.With.WITH_LEAST);
		if (user == null) {
			throw new ClientParameterInvalidErrorCodeException(
					ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(), "user not found");
		}

		Update update = new UserPO.Update(user.getId());
		update.setPassword(passwordEncoder.encode(dto.getPassword()));

		doUpdate(update);
	}

	public void updatePassword(UpdatePasswordDTO dto) throws ErrorCodeException {
		UserPO user = findOne(dto.getId(), UserQuery.With.WITH_LEAST);
		if (user == null) {
			throw new ClientParameterInvalidErrorCodeException(
					ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(), "user not found");
		}
		Long cUserId = UserUtils.getUserId();
		if (!user.getId().equals(cUserId)) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.NOT_FOUND,
					"Not Found, Ownership");
		}
		if (!matchesPassword(dto.getPasswordOld(), user.getPassword())) {
			throw new ClientParameterInvalidErrorCodeException(
					ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(),
					"Original password not match");
		}

		Update update = new UserPO.Update(user.getId());
		update.setPassword(passwordEncoder.encode(dto.getPasswordNew()));

		doUpdate(update);
	}

	public void enable(Long id) throws ErrorCodeException {
		Update update = new UserPO.Update(id);
		update.setActived(true);

		doUpdate(update);
	}

	public void disable(Long id) throws ErrorCodeException {
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
			throw new ClientParameterInvalidErrorCodeException(
					ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(),
					new SQLIntegrityConstraintException(e).getMessage());
		}

		if (i == 0) {
			throw new ClientParameterInvalidErrorCodeException(
					ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(), "user not found");
		}
	}

	public boolean matchesPassword(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
}
