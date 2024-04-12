package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.entity.AddressBook;
import com.qfnu.reggie.mapper.AddressBookMapper;
import com.qfnu.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
