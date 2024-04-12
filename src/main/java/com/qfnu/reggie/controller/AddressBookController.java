package com.qfnu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qfnu.reggie.common.BaseContext;
import com.qfnu.reggie.common.R;
import com.qfnu.reggie.entity.AddressBook;
import com.qfnu.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;



    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {

        log.info(addressBook.toString());

        // 生成一个用户id
        addressBook.setUserId(BaseContext.getCurrentId());

        //保存到address_book表
        addressBookService.save(addressBook);

        return R.success(addressBook);
    }



    /**
     * 查询指定用户的所有地址
     * @param addressBook 用addressBook接收前端发来的数据
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> list = addressBookService.list(queryWrapper);

        return R.success(list);
    }




    /**
     * 设置默认地址
     * 因为默认地址只有一个，所以需要根据用户查询到他所有的地址，首先把所有地址is_default设为0即不是默认地址，再把当前的地址设为默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {

        log.info(addressBook.toString());

        //把该用户所有的地址全部设置为非默认地址即is_default为0
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        wrapper.set(AddressBook::getIsDefault, 0);
        // update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);

        //参数是前端传过来的要设置的地址
        //把当前地址设为默认地址
        addressBook.setIsDefault(1);
        // update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);

        return R.success(addressBook);
    }




    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("default")
    public R<AddressBook> getDefault() {
        //查询当前用户的默认地址
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        //判断
        if (null == addressBook) {
            return R.error("没有找到该用户");
        }else {
            return R.success(addressBook);
        }
    }



    /**
     * 根据id查询地址——用于数据回显
     * 点击地址右边的修改图标会请求该方法
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);

        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该用户！");
        }
    }



    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<AddressBook> update(@RequestBody AddressBook addressBook) {

        log.info(addressBook.toString());

        if (addressBook == null) {
            return R.error("请求异常！");
        }

        addressBookService.updateById(addressBook);

        return R.success(addressBook);
    }



    /**
     * 删除地址
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") Long id) {
        if (id == null) {
            return R.error("请求异常！");
        }

        // 查询该用户的该地址
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getId, id);      // 此id是地址id

        addressBookService.remove(queryWrapper);

        return R.success("删除地址成功！");
    }

}













