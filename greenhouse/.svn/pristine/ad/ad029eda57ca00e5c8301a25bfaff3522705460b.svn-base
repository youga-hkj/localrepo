package com.newcoming.greenhouse.view.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.newcoming.greenhouse.base.BaseAction;
import com.newcoming.greenhouse.domain.Role;
import com.newcoming.greenhouse.domain.User;
import com.newcoming.greenhouse.util.QueryHelper;
import com.newcoming.greenhouse.util.TextUtil;

import com.opensymphony.xwork2.ActionContext;

@SuppressWarnings("serial")
@Controller
@Scope("prototype")
public class UserAction2 extends BaseAction<User> {

//	private Long departmentId;
	private Long[] greenhousesIds;
	private Long[] roleIds;
	
	/** 首页 */
	public String index() throws Exception {
		return "index";
	}
	
	/** 列表 */
	public String list() throws Exception {

		// 准备分页信息
		new QueryHelper(User.class, "u")//
			.addCondition(!TextUtil.isEmpty(model.getLoginName()), "u.loginName like ?", model.getLoginName())
			.addCondition(!TextUtil.isEmpty(model.getName()), "u.name like ?", model.getName())
			.addCondition("u.loginName!='admin'")
			.addOrderProperty("u.id", false)
			.preparePageBean(userService, pageNum, pageSize);
		return "list";
	}

	/** 查询界面 */
	public String queryUI() throws Exception {
		return "queryUI";
	}
	
	
	/** 删除 */
	public String delete() throws Exception {
		userService.delete(model.getId());
		return "toList";
	}

	/** 添加页面 */
	public String addUI() throws Exception {
		// 准备数据, departmentList
	//	List<Department> topList = departmentService.findTopList();
	//	List<Department> departmentList = DepartmentUtils.getAllDepartments(topList);
//		ActionContext.getContext().put("departmentList", departmentList);
//
//		
//		// 准备数据, roleList
//		List<Role> roleList = roleService.findAll();
//		ActionContext.getContext().put("roleList", roleList);

		return "saveUI";
	}

	
	/** 添加 */
	public String add() throws Exception {
		// 封装到对象中（当model是实体类型时，也可以使用model，但要设置未封装的属性）
		
		User user = userService.getbyLoginName(model.getLoginName());
		if(user!=null){
			returnDialog("登录用户名已经存在，请重新输入");
			return null;
		}else{
			// >> 设置关联的角色
			List<Role> roleList = roleService.getByIds(roleIds);
			model.setRoles(new HashSet<Role>(roleList));
			// >> 设置默认密码为1234（要使用MD5摘要）
			String md5Digest = DigestUtils.md5Hex("1234");
			model.setPassword(md5Digest);
			model.setAddBy(getCurrentUser());//设置当前操作员
			//(getCurrentUser().getId());//设置当前操作员
			model.setUserTime(new Date(System.currentTimeMillis()));
	
			// 保存到数据库
			userService.save(model);
			return "toList";
		}
	}

	/** 修改页面 */
	public String editUI() throws Exception {
		// 准备数据, departmentList
//		List<Department> topList = departmentService.findTopList();
//		List<Department> departmentList = DepartmentUtils.getAllDepartments(topList);
//		ActionContext.getContext().put("departmentList", departmentList);

		// 准备数据, roleList
		List<Role> roleList = roleService.findAll();
		ActionContext.getContext().put("roleList", roleList);

		// 准备回显的数据
		User user = userService.getById(model.getId());
		ActionContext.getContext().getValueStack().push(user);
//		if (user.getDepartment() != null) {
//			departmentId = user.getDepartment().getId();
//		}
		if (user.getRoles() != null) {
			roleIds = new Long[user.getRoles().size()];
			int index = 0;
			for (Role role : user.getRoles()) {
				roleIds[index++] = role.getId();
			}
		}
		return "saveUI";
	}

	/** 修改 */
	public String edit() throws Exception {
		// 1，从数据库中取出原对象
		User user = userService.getById(model.getId());

		// 2，设置要修改的属性
		user.setLoginName(model.getLoginName());
		user.setName(model.getName());
		user.setGender(model.getGender());
		user.setPhoneNumber(model.getPhoneNumber());
		user.setEmail(model.getEmail());
		user.setRemark(model.getRemark());
		// >> 设置所属部门
		//user.setDepartment(departmentService.getById(departmentId));
		// >> 设置关联的岗位
		List<Role> roleList = roleService.getByIds(roleIds);
		user.setRoles(new HashSet<Role>(roleList));
//		user.setFinger(model.getFinger());
		user.setChaBy(getCurrentUser());//设置当前操作员ID
//		(getCurrentUser().getChaBy());//原来设置当前操作员ID
		user.setUserTime(new Date(System.currentTimeMillis()));
		// 3，更新到数据库
		userService.update(user);

		return "toList";
	}

	/** 初始化密码为1234 */
	public String initPassword() throws Exception {
		// 1，从数据库中取出原对象
		User user = userService.getById(model.getId());

		// 2，设置要修改的属性（要使用MD5摘要）
		String md5Digest = DigestUtils.md5Hex("1234");
		user.setPassword(md5Digest);

		// 3，更新到数据库
		userService.update(user);

		return "toList";
	}

	/** 登录页面 */
	public String loginUI() throws Exception {
		return "loginUI";
	}

	/** 登录 */
	public String login() throws Exception {
		User user = userService.findByLoginNameAndPassword(model.getLoginName().trim(), model.getPassword());
		if (user == null) {
			addFieldError("password", "用户名或密码不正确！");
			return "loginUI";
		} else {
			// 登录用户
			ActionContext.getContext().getSession().put("user", user);
			return "toIndex";
		}
	}

	/** 注销 */
	public String logout() throws Exception {
		ActionContext.getContext().getSession().remove("user");
		return "logout";
	}

	
	/** 修改密码UI */
	public String editPwdUI() throws Exception {
		User user = userService.getById(model.getId());
		ActionContext.getContext().getValueStack().push(user);
		return "editPwdUI";
	}
	
	/** 修改密码 */
	public String editPwd() throws Exception {
		// 1，从数据库中取出原对象
		User user = userService.getById(model.getId());
		// 2，修改密码
		user.setPassword(DigestUtils.md5Hex(model.getPassword()));
		userService.update(user);
		user = userService.findByLoginNameAndPassword(model.getLoginName(), model.getPassword());
		if(user!=null){
			returnDialog("修改密码失败，重新修改！");
			return null;
		}else{
			returnDialog("修改密码成功！");
			return null;
		}
	}
	
	/** ajax 获取用户指纹 */
/*	public String getFingers() throws Exception {
		try {
			String fingers = "";
			StringBuffer sb = new StringBuffer();
			request.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=utf-8");
			List<User> users = userService.findAll();
			if (users.size() > 0) {
				for (User user : users) {
					sb.append("[");
					sb.append("\""+user.getLoginName()+"\"");
					sb.append(",");
					sb.append("\""+user.getFinger()+"\"");
					sb.append("]");
					sb.append(",");
				}
				fingers = sb.toString().substring(0, sb.toString().length()-1);
			}
			PrintWriter out = response.getWriter();
			out.println("["+fingers+"]");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	*/
	/** ajax 校验用户原密码 */
	public String checkOldPwd() throws Exception {
		try {
			String status="";
			request.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=utf-8");
			User user = userService.findByLoginNameAndPassword(model.getLoginName(),DigestUtils.md5Hex(request.getParameter("oldPwd")));
			if (user!=null) {
				status="1";	// 与数据库一致
			}else{
				status="0";
			}
			PrintWriter out = response.getWriter();
			out.println("["+status+"]");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// -----------------------------
/*
	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}
*/
	public Long[] getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(Long[] roleIds) {
		this.roleIds = roleIds;
	}

	public Long[] getGreenhousesIds() {
		return greenhousesIds;
	}

	public void setGreenhousesIds(Long[] greenhousesIds) {
		this.greenhousesIds = greenhousesIds;
	}

}
