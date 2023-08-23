**FullCondtions(exps, name, p, loc)**= $EclipseConditons(exps, name, p) \wedge AdditionalConditons(exps,p, loc)$ 

**EclipseConditons(exps, name, p)** = $ShareIdenticalNameBinding(exps,p) \wedge NoCompilationErrors(exps,name,p) \wedge (\forall exp \in exps,  NotLeftValue(exp)  \wedge NotDeclaration (exp) \wedge NotParameter (exp) \wedge NotEnumInSwitchCase (exp) \wedge NotInComments (exp) \wedge NotInAnnotations (exp) \wedge NotReferringToLocalVariableFromForExpression (exp) \wedge NotUsedInForInitializerOrUpdater \wedge NotANamePropertyInParentLocation (exp) \wedge NotVoid (exp) \wedge NotSingleNullLiteral (exp) \wedge IsCompleteExp (exp) \wedge InMethodOrInLambdaOrInInitializerBody (exp) )  \wedge NoDuplicateNames(name,p))$ 

**AdditionalConditons(exps, p, loc)**= $ExpCondition(exps,p) \wedge  ExceptionAvoidingConditions(exps,p,loc) \wedge ChangedExpValueAvoidingConditions(exps,p,loc)$

**ExpCondition(exps,p)**=$\forall exp \in exps, NoUpdateToFieldsVariablesOrArguments(exp,p) \wedge NoOutputs (exp) \wedge NoInputs(exp)$   

**ExceptionAvoidingConditions(exps,p,loc)**=$\forall exp \in exps,  AvoidNullPointerException(exp,p,loc) \wedge  AvoidTypeCastException(exp,p,loc)$

**AvoidNullPointerException(exp,p,loc)**=($\forall stm \in Path(loc,  exp, p)) (\forall NullCheck \in Conditions(stm)), Expression(NullCheck) \notin SubExpressions(exp)$

**AvoidTypeCastException(exp,p,loc)**=$(\forall stm \in Path(loc,exp,p))  (\forall TypeCheck \in Conditions(stm))(\forall typeCast\in TypeCasts(exp)),  CastedInstance(typeCast) != CheckedInstance(TypeCheck)$

**ChangedExpValueAvoidingConditions(exps,p,loc)**=($\forall exp_1, exp_2 \in exps)(\forall var \in (UpdatedByExePath(exp_1,exp_2,p)\cup UpdatedByExePath(loc, exp_1,p) \cup UpdatedByExePath(loc, exp_2, p)) , var\notin (ReadBy(exp_1) \cup ReadBy(exp_2))$