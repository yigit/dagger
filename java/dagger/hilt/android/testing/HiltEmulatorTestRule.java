/*
 * Copyright (C) 2020 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.hilt.android.testing;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import dagger.hilt.android.internal.testing.TestApplicationComponentManager;
import dagger.hilt.android.internal.testing.TestApplicationComponentManagerHolder;
import dagger.hilt.android.internal.testing.TestInstanceHolder;
import dagger.hilt.internal.Preconditions;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A Junit {@code TestRule} that's installed in all Hilt Emulator tests.
 *
 * <p>This rule is required. The Dagger component will not be created without this test rule.
 */
public final class HiltEmulatorTestRule implements TestRule {
  private final RuleChain rules;
  private final Object testClassInstance;

  /** Creates a new instance of the rules. Tests should pass {@code this}. */
  public HiltEmulatorTestRule(Object testClassInstance) {
    this.testClassInstance = testClassInstance;

    Context applicationContext = InstrumentationRegistry.getTargetContext();
    rules = RuleChain.outerRule(new MarkThatRulesRanRule(applicationContext));

    if (applicationContext instanceof TestInstanceHolder) {
      ((TestInstanceHolder) applicationContext).setTestInstance(testClassInstance);
    }

    if (applicationContext instanceof TestApplicationComponentManagerHolder) {
      Object componentManager =
          ((TestApplicationComponentManagerHolder) applicationContext).componentManager();
      Preconditions.checkState(componentManager instanceof TestApplicationComponentManager, "");
      ((TestApplicationComponentManager) componentManager).setBindValueCalled();
    }
  }

  @Override public Statement apply(Statement baseStatement, Description description) {
    Preconditions.checkState(
        description.getTestClass().isInstance(testClassInstance),
        "HiltEmulatorTestRule was constructed with an "
            + "argument that was not an instance of the test class");
    return rules.apply(baseStatement, description);
  }
}
