# Sign Analysis — Software Analysis Course (University of Passau)

This project implements a **sign analysis tool** for Java programs, based on **data-flow analysis**. The goal is to analyze how integer values propagate through a program and detect potential runtime issues like division by zero or negative array indexing.

## Overview

* The analysis tracks **integer values** using abstract sign information:

    * **(−)** Negative
    * **0** Zero
    * **(+)** Positive

* The sign information is propagated using a **lattice-based abstraction**.

* Implemented in **Java** using the **ASM bytecode instrumentation framework**.

For the complete task description, please refer to [Sign_Analysis(SS25).pdf](Sign_Analysis%28SS25%29.pdf)

## Warnings & Errors

* **Warning:**

    * When array index *might* be negative.
    * When divisor *might* be zero.

* **Error:**

    * When array index is *definitely* negative.
    * When divisor is *definitely* zero.

## Scope

* Focuses exclusively on **`int` values** in the analyzed Java programs.
* Ignores other types and operations unrelated to `int`.

## Achieved Test Coverage

* **Line Coverage:** 85%
* **Branch Coverage:** 75%
* **Mutation Score:** 80%
