package com.demo.controllers;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
@Suite
@SelectPackages("com.demo.repositories")
public class RepositoryTestSuite {
}
