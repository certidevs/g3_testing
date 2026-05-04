package com.demo;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("com.demo.controllers")
public class ControllerTestSuite {
}
