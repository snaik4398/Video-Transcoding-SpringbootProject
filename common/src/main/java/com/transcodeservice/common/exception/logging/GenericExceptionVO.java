package com.transcodeservice.common.exception.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenericExceptionVO {
	private long errorCode;
	private String exception;
}
