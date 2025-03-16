package com.fr.dp.service.filter;

import com.fr.dp.dataservice.handler.HandlerConfigEntity;
import com.fr.dp.dataservice.handler.pre.PreHandler;

/**
 * This class created on 2023/7/7
 *
 * @author Kuifang.Liu
 */
public abstract class AuthHandler<E extends HandlerConfigEntity> implements PreHandler<E> {
}
