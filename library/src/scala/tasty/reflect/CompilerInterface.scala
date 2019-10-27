package scala.tasty.reflect // TODO move to scala.internal.tasty.reflect

import scala.quoted.QuoteContext
import scala.tasty.Reflection
import scala.runtime.quoted.Unpickler

/** Tasty reflect abstract types
 *
 *  ```none
 *
 *  +- Tree -+- PackageClause
 *           +- Import
 *           +- Statement -+- Definition --+- PackageDef
 *           |             |               +- ClassDef
 *           |             |               +- TypeDef
 *           |             |               +- DefDef
 *           |             |               +- ValDef
 *           |             |
 *           |             +- Term --------+- Ref -+- Ident
 *           |                             |       +- Select
 *           |                             |
 *           |                             +- Literal
 *           |                             +- This
 *           |                             +- New
 *           |                             +- NamedArg
 *           |                             +- Apply
 *           |                             +- TypeApply
 *           |                             +- Super
 *           |                             +- Typed
 *           |                             +- Assign
 *           |                             +- Block
 *           |                             +- Closure
 *           |                             +- If
 *           |                             +- Match
 *           |                             +- ImpliedMatch
 *           |                             +- Try
 *           |                             +- Return
 *           |                             +- Repeated
 *           |                             +- Inlined
 *           |                             +- SelectOuter
 *           |                             +- While
 *           |
 *           |
 *           +- TypeTree ----+- Inferred
 *           |               +- TypeIdent
 *           |               +- TypeSelect
 *           |               +- Projection
 *           |               +- Singleton
 *           |               +- Refined
 *           |               +- Applied
 *           |               +- Annotated
 *           |               +- MatchTypeTree
 *           |               +- ByName
 *           |               +- LambdaTypeTree
 *           |               +- TypeBind
 *           |               +- TypeBlock
 *           |
 *           +- TypeBoundsTree
 *           +- WildcardTypeTree
*            |
 *           +- CaseDef
 *           +- TypeCaseDef
 *           |
 *           +- Bind
 *           +- Unapply
 *           +- Alternatives
 *
 *                   +- NoPrefix
 *  +- TypeOrBounds -+- TypeBounds
 *                   |
 *                   +- Type -------+- ConstantType
 *                                  +- TermRef
 *                                  +- TypeRef
 *                                  +- SuperType
 *                                  +- Refinement
 *                                  +- AppliedType
 *                                  +- AnnotatedType
 *                                  +- AndType
 *                                  +- OrType
 *                                  +- MatchType
 *                                  +- ByNameType
 *                                  +- ParamRef
 *                                  +- ThisType
 *                                  +- RecursiveThis
 *                                  +- RecursiveType
 *                                  +- LambdaType[ParamInfo <: TypeOrBounds] -+- MethodType
 *                                                                            +- PolyType
 *                                                                            +- TypeLambda
 *
 *  +- ImportSelector -+- SimpleSelector
 *                     +- RenameSelector
 *                     +- OmitSelector
 *
 *  +- Id
 *
 *  +- Signature
 *
 *  +- Position
 *
 *  +- Comment
 *
 *  +- Constant
 *
 *  +- Symbol
 *
 *  +- Flags
 *
 *  ```
 */
trait CompilerInterface {

  /** Context of the macro expansion */
  def rootContext: Context

  /** Root position of this tasty context. For macros it corresponds to the expansion site. */
  def rootPosition: Position

  def settings: Settings

  //
  // QUOTE UNPICKLING
  //

  /** Unpickle `repr` which represents a pickled `Expr` tree,
   *  replacing splice nodes with `args`
   */
  def unpickleExpr(repr: Unpickler.PickledQuote, args: Unpickler.PickledExprArgs): scala.quoted.Expr[_]

  /** Unpickle `repr` which represents a pickled `Type` tree,
   *  replacing splice nodes with `args`
   */
  def unpickleType(repr: Unpickler.PickledQuote, args: Unpickler.PickledTypeArgs): scala.quoted.Type[_]

  //
  // CONTEXT
  //

  /** Compilation context */
  type Context <: AnyRef

  /** Returns the owner of the context */
  def Context_owner(self: Context): Symbol

  /** Returns the source file being compiled. The path is relative to the current working directory. */
  def Context_source(self: Context): java.nio.file.Path

  def Context_GADT_setFreshGADTBounds(self: Context): Context
  def Context_GADT_addToConstraint(self: Context)(syms: List[Symbol]): Boolean
  def Context_GADT_approximation(self: Context)(sym: Symbol, fromBelow: Boolean): Type

  //
  // REPORTING
  //

  /** Report a compilation error with the given message at the given position */
  def error(msg: => String, pos: Position)(given ctx: Context): Unit

  /** Report a compilation error with the given message at the given position range */
  def error(msg: => String, source: SourceFile, start: Int, end: Int)(given ctx: Context): Unit

  /** Report a compilation warning with the given message at the given position */
  def warning(msg: => String, pos: Position)(given ctx: Context): Unit

  /** Report a compilation warning with the given message at the given position range */
  def warning(msg: => String, source: SourceFile, start: Int, end: Int)(given ctx: Context): Unit

  //
  // Settings
  //

  /** Settings */
  type Settings <: AnyRef

  def Settings_color(self: Settings): Boolean

  //
  // TREES
  //

  /** Tree representing code written in the source */
  type Tree <: AnyRef

  def Tree_pos(self: Tree)(given ctx: Context): Position
  def Tree_symbol(self: Tree)(given ctx: Context): Symbol

  /** Tree representing a pacakage clause in the source code */
  type PackageClause <: Tree

  def matchPackageClause(tree: Tree)(given ctx: Context): Option[PackageClause]

  def PackageClause_pid(self: PackageClause)(given ctx: Context): Ref
  def PackageClause_stats(self: PackageClause)(given ctx: Context): List[Tree]

  def PackageClause_apply(pid: Ref, stats: List[Tree])(given ctx: Context): PackageClause

  def PackageClause_copy(original: PackageClause)(pid: Ref, stats: List[Tree])(given ctx: Context): PackageClause

  /** Tree representing a statement in the source code */
  type Statement <: Tree

  def matchStatement(tree: Tree)(given ctx: Context): Option[Statement]

  /** Tree representing an import in the source code */
  type Import <: Statement

  def matchImport(tree: Tree)(given ctx: Context): Option[Import]

  def Import_implied(self: Import): Boolean
  def Import_expr(self: Import)(given ctx: Context): Term
  def Import_selectors(self: Import)(given ctx: Context): List[ImportSelector]

  def Import_apply(iexpr: Term, selectors: List[ImportSelector])(given ctx: Context): Import

  def Import_copy(original: Import)(expr: Term, selectors: List[ImportSelector])(given ctx: Context): Import

  /** Tree representing a definition in the source code. It can be `PackageDef`, `ClassDef`, `TypeDef`, `DefDef` or `ValDef` */
  type Definition <: Statement

  def matchDefinition(tree: Tree)(given ctx: Context): Option[Definition]

  def Definition_name(self: Definition)(given ctx: Context): String

  /** Tree representing a package definition. This includes definitions in all source files */
  type PackageDef <: Definition

  def matchPackageDef(tree: Tree)(given ctx: Context): Option[PackageDef]

  def PackageDef_owner(self: PackageDef)(given ctx: Context): PackageDef
  def PackageDef_members(self: PackageDef)(given ctx: Context): List[Statement]

  /** Tree representing a class definition. This includes annonymus class definitions and the class of a module object */
  type ClassDef <: Definition

  def matchClassDef(tree: Tree)(given ctx: Context): Option[ClassDef]

  def ClassDef_constructor(self: ClassDef)(given ctx: Context): DefDef
  def ClassDef_parents(self: ClassDef)(given ctx: Context): List[Tree/* Term | TypeTree */]
  def ClassDef_derived(self: ClassDef)(given ctx: Context): List[TypeTree]
  def ClassDef_self(self: ClassDef)(given ctx: Context): Option[ValDef]
  def ClassDef_body(self: ClassDef)(given ctx: Context): List[Statement]

  def ClassDef_copy(original: ClassDef)(name: String, constr: DefDef, parents: List[Tree/* Term | TypeTree */], derived: List[TypeTree], selfOpt: Option[ValDef], body: List[Statement])(given ctx: Context): ClassDef

  /** Tree representing a type (paramter or member) definition in the source code */
  type TypeDef <: Definition

  def matchTypeDef(tree: Tree)(given ctx: Context): Option[TypeDef]

  def TypeDef_rhs(self: TypeDef)(given ctx: Context): Tree /*TypeTree | TypeBoundsTree*/

  def TypeDef_apply(symbol: Symbol)(given ctx: Context): TypeDef
  def TypeDef_copy(original: TypeDef)(name: String, rhs: Tree /*TypeTree | TypeBoundsTree*/)(given ctx: Context): TypeDef

  /** Tree representing a method definition in the source code */
  type DefDef <: Definition

  def matchDefDef(tree: Tree)(given ctx: Context): Option[DefDef]

  def DefDef_typeParams(self: DefDef)(given ctx: Context): List[TypeDef]
  def DefDef_paramss(self: DefDef)(given ctx: Context): List[List[ValDef]]
  def DefDef_returnTpt(self: DefDef)(given ctx: Context): TypeTree
  def DefDef_rhs(self: DefDef)(given ctx: Context): Option[Term]

  def DefDef_apply(symbol: Symbol, rhsFn: List[Type] => List[List[Term]] => Option[Term])(given ctx: Context): DefDef
  def DefDef_copy(original: DefDef)(name: String, typeParams: List[TypeDef], paramss: List[List[ValDef]], tpt: TypeTree, rhs: Option[Term])(given ctx: Context): DefDef

  /** Tree representing a value definition in the source code This inclues `val`, `lazy val`, `var`, `object` and parameter definitions. */
  type ValDef <: Definition

  def matchValDef(tree: Tree)(given ctx: Context): Option[ValDef]

  def ValDef_tpt(self: ValDef)(given ctx: Context): TypeTree
  def ValDef_rhs(self: ValDef)(given ctx: Context): Option[Term]

  def ValDef_apply(symbol: Symbol, rhs: Option[Term])(given ctx: Context): ValDef
  def ValDef_copy(original: ValDef)(name: String, tpt: TypeTree, rhs: Option[Term])(given ctx: Context): ValDef

  /** Tree representing an expression in the source code */
  type Term <: Statement

  def matchTerm(tree: Tree)(given ctx: Context): Option[Term]

  def Term_tpe(self: Term)(given ctx: Context): Type
  def Term_underlyingArgument(self: Term)(given ctx: Context): Term
  def Term_underlying(self: Term)(given ctx: Context): Term

  /** Tree representing a reference to definition */
  type Ref <: Term

  def matchRef(tree: Tree)(given ctx: Context): Option[Ref]

  def Ref_apply(sym: Symbol)(given ctx: Context): Ref

  /** Tree representing a reference to definition with a given name */
  type Ident <: Ref

  def matchIdent(tree: Tree)(given ctx: Context): Option[Ident]

  def Ident_name(self: Ident)(given ctx: Context): String

  def Ident_apply(tmref: TermRef)(given ctx: Context): Term
  def Ident_copy(original: Tree)(name: String)(given ctx: Context): Ident

  /** Tree representing a selection of definition with a given name on a given prefix */
  type Select <: Ref

  def matchSelect(tree: Tree)(given ctx: Context): Option[Select]

  def Select_qualifier(self: Select)(given ctx: Context): Term
  def Select_name(self: Select)(given ctx: Context): String
  def Select_signature(self: Select)(given ctx: Context): Option[Signature]

  def Select_apply(qualifier: Term, symbol: Symbol)(given ctx: Context): Select
  def Select_unique(qualifier: Term, name: String)(given ctx: Context): Select
  // TODO rename, this returns an Apply and not a Select
  def Select_overloaded(qualifier: Term, name: String, targs: List[Type], args: List[Term])(given ctx: Context): Apply
  def Select_copy(original: Tree)(qualifier: Term, name: String)(given ctx: Context): Select

  /** Tree representing a literal value in the source code */
  type Literal <: Term

  def matchLiteral(tree: Tree)(given ctx: Context): Option[Literal]

  def Literal_constant(self: Literal)(given ctx: Context): Constant

  def Literal_apply(constant: Constant)(given ctx: Context): Literal
  def Literal_copy(original: Tree)(constant: Constant)(given ctx: Context): Literal

  /** Tree representing `this` in the source code */
  type This <: Term

  def matchThis(tree: Tree)(given ctx: Context): Option[This]

  def This_id(self: This)(given ctx: Context): Option[Id]

  def This_apply(cls: Symbol)(given ctx: Context): This
  def This_copy(original: Tree)(qual: Option[Id])(given ctx: Context): This

  /** Tree representing `new` in the source code */
  type New <: Term

  def matchNew(tree: Tree)(given ctx: Context): Option[New]

  def New_tpt(self: New)(given ctx: Context): TypeTree

  def New_apply(tpt: TypeTree)(given ctx: Context): New
  def New_copy(original: Tree)(tpt: TypeTree)(given ctx: Context): New

  /** Tree representing an argument passed with an explicit name. Such as `arg1 = x` in `foo(arg1 = x)` */
  type NamedArg <: Term

  def matchNamedArg(tree: Tree)(given ctx: Context): Option[NamedArg]

  def NamedArg_name(self: NamedArg)(given ctx: Context): String
  def NamedArg_value(self: NamedArg)(given ctx: Context): Term

  def NamedArg_apply(name: String, arg: Term)(given ctx: Context): NamedArg
  def NamedArg_copy(tree: NamedArg)(name: String, arg: Term)(given ctx: Context): NamedArg

  /** Tree an application of arguments. It represents a single list of arguments, multiple argument lists will have nested `Apply`s */
  type Apply <: Term

  def matchApply(tree: Tree)(given ctx: Context): Option[Apply]

  def Apply_fun(self: Apply)(given ctx: Context): Term
  def Apply_args(self: Apply)(given ctx: Context): List[Term]

  def Apply_apply(fn: Term, args: List[Term])(given ctx: Context): Apply
  def Apply_copy(original: Tree)(fun: Term, args: List[Term])(given ctx: Context): Apply

  /** Tree an application of type arguments */
  type TypeApply <: Term

  def matchTypeApply(tree: Tree)(given ctx: Context): Option[TypeApply]

  def TypeApply_fun(self: TypeApply)(given ctx: Context): Term
  def TypeApply_args(self: TypeApply)(given ctx: Context): List[TypeTree]

  def TypeApply_apply(fn: Term, args: List[TypeTree])(given ctx: Context): TypeApply
  def TypeApply_copy(original: Tree)(fun: Term, args: List[TypeTree])(given ctx: Context): TypeApply

  /** Tree representing `super` in the source code */
  type Super <: Term

  def matchSuper(tree: Tree)(given ctx: Context): Option[Super]

  def Super_qualifier(self: Super)(given ctx: Context): Term
  def Super_id(self: Super)(given ctx: Context): Option[Id]

  def Super_apply(qual: Term, mix: Option[Id])(given ctx: Context): Super
  def Super_copy(original: Tree)(qual: Term, mix: Option[Id])(given ctx: Context): Super

  /** Tree representing a type ascription `x: T` in the source code */
  type Typed <: Term

  def matchTyped(tree: Tree)(given ctx: Context): Option[Typed]

  def Typed_expr(self: Typed)(given ctx: Context): Term
  def Typed_tpt(self: Typed)(given ctx: Context): TypeTree

  def Typed_apply(expr: Term, tpt: TypeTree)(given ctx: Context): Typed
  def Typed_copy(original: Tree)(expr: Term, tpt: TypeTree)(given ctx: Context): Typed

  /** Tree representing an assignment `x = y` in the source code */
  type Assign <: Term

  def matchAssign(tree: Tree)(given ctx: Context): Option[Assign]

  def Assign_lhs(self: Assign)(given ctx: Context): Term
  def Assign_rhs(self: Assign)(given ctx: Context): Term

  def Assign_apply(lhs: Term, rhs: Term)(given ctx: Context): Assign
  def Assign_copy(original: Tree)(lhs: Term, rhs: Term)(given ctx: Context): Assign

  /** Tree representing a block `{ ... }` in the source code */
  type Block <: Term

  def matchBlock(tree: Tree)(given ctx: Context): Option[Block]

  def Block_statements(self: Block)(given ctx: Context): List[Statement]
  def Block_expr(self: Block)(given ctx: Context): Term

  def Block_apply(stats: List[Statement], expr: Term)(given ctx: Context): Block
  def Block_copy(original: Tree)(stats: List[Statement], expr: Term)(given ctx: Context): Block

  /** A lambda `(...) => ...` in the source code is represented as
   *  a local method and a closure:
   *
   *  {
   *    def m(...) = ...
   *    closure(m)
   *  }
   *
   */
  type Closure <: Term

  def matchClosure(tree: Tree)(given ctx: Context): Option[Closure]

  def Closure_meth(self: Closure)(given ctx: Context): Term
  def Closure_tpeOpt(self: Closure)(given ctx: Context): Option[Type]

  def Closure_apply(meth: Term, tpe: Option[Type])(given ctx: Context): Closure
  def Closure_copy(original: Tree)(meth: Tree, tpe: Option[Type])(given ctx: Context): Closure

  /** Tree representing an if/then/else `if (...) ... else ...` in the source code */
  type If <: Term

  def matchIf(tree: Tree)(given ctx: Context): Option[If]

  def If_cond(self: If)(given ctx: Context): Term
  def If_thenp(self: If)(given ctx: Context): Term
  def If_elsep(self: If)(given ctx: Context): Term

  def If_apply(cond: Term, thenp: Term, elsep: Term)(given ctx: Context): If
  def If_copy(original: Tree)(cond: Term, thenp: Term, elsep: Term)(given ctx: Context): If

  /** Tree representing a pattern match `x match  { ... }` in the source code */
  type Match <: Term

  def matchMatch(tree: Tree)(given ctx: Context): Option[Match]

  def Match_scrutinee(self: Match)(given ctx: Context): Term
  def Match_cases(self: Match)(given ctx: Context): List[CaseDef]

  def Match_apply(selector: Term, cases: List[CaseDef])(given ctx: Context): Match
  def Match_copy(original: Tree)(selector: Term, cases: List[CaseDef])(given ctx: Context): Match

  /** Tree representing a pattern match `delegate match  { ... }` in the source code */
  type ImpliedMatch <: Term

  def matchImplicitMatch(tree: Tree)(given ctx: Context): Option[ImpliedMatch]

  def ImplicitMatch_cases(self: ImpliedMatch)(given ctx: Context): List[CaseDef]

  def ImplicitMatch_apply(cases: List[CaseDef])(given ctx: Context): ImpliedMatch
  def ImplicitMatch_copy(original: Tree)(cases: List[CaseDef])(given ctx: Context): ImpliedMatch

  /** Tree representing a tyr catch `try x catch { ... } finally { ... }` in the source code */
  type Try <: Term

  def matchTry(tree: Tree)(given ctx: Context): Option[Try]

  def Try_body(self: Try)(given ctx: Context): Term
  def Try_cases(self: Try)(given ctx: Context): List[CaseDef]
  def Try_finalizer(self: Try)(given ctx: Context): Option[Term]

  def Try_apply(expr: Term, cases: List[CaseDef], finalizer: Option[Term])(given ctx: Context): Try
  def Try_copy(original: Tree)(expr: Term, cases: List[CaseDef], finalizer: Option[Term])(given ctx: Context): Try

  /** Tree representing a `return` in the source code */
  type Return <: Term

  def matchReturn(tree: Tree)(given ctx: Context): Option[Return]

  def Return_expr(self: Return)(given ctx: Context): Term

  def Return_apply(expr: Term)(given ctx: Context): Return
  def Return_copy(original: Tree)(expr: Term)(given ctx: Context): Return

  /** Tree representing a variable argument list in the source code */
  type Repeated <: Term

  def matchRepeated(tree: Tree)(given ctx: Context): Option[Repeated]

  def Repeated_elems(self: Repeated)(given ctx: Context): List[Term]
  def Repeated_elemtpt(self: Repeated)(given ctx: Context): TypeTree

  def Repeated_apply(elems: List[Term], elemtpt: TypeTree)(given ctx: Context): Repeated
  def Repeated_copy(original: Tree)(elems: List[Term], elemtpt: TypeTree)(given ctx: Context): Repeated

  /** Tree representing the scope of an inlined tree */
  type Inlined <: Term

  def matchInlined(tree: Tree)(given ctx: Context): Option[Inlined]

  def Inlined_call(self: Inlined)(given ctx: Context): Option[Tree/* Term | TypeTree */]
  def Inlined_bindings(self: Inlined)(given ctx: Context): List[Definition]
  def Inlined_body(self: Inlined)(given ctx: Context): Term

  def Inlined_apply(call: Option[Tree/* Term | TypeTree */], bindings: List[Definition], expansion: Term)(given ctx: Context): Inlined
  def Inlined_copy(original: Tree)(call: Option[Tree/* Term | TypeTree */], bindings: List[Definition], expansion: Term)(given ctx: Context): Inlined

  /** Tree representing a selection of definition with a given name on a given prefix and number of nested scopes of inlined trees */
  type SelectOuter <: Term

  def matchSelectOuter(tree: Tree)(given ctx: Context): Option[SelectOuter]

  def SelectOuter_qualifier(self: SelectOuter)(given ctx: Context): Term
  def SelectOuter_level(self: SelectOuter)(given ctx: Context): Int

  def SelectOuter_apply(qualifier: Term, name: String, levels: Int)(given ctx: Context): SelectOuter
  def SelectOuter_copy(original: Tree)(qualifier: Term, name: String, levels: Int)(given ctx: Context): SelectOuter

  /** Tree representing a while loop */
  type While <: Term

  def matchWhile(tree: Tree)(given ctx: Context): Option[While]

  def While_cond(self: While)(given ctx: Context): Term
  def While_body(self: While)(given ctx: Context): Term

  def While_apply(cond: Term, body: Term)(given ctx: Context): While
  def While_copy(original: Tree)(cond: Term, body: Term)(given ctx: Context): While

  /** Type tree representing a type written in the source */
  type TypeTree <: Tree

  def matchTypeTree(tree: Tree)(given ctx: Context): Option[TypeTree]

  def TypeTree_tpe(self: TypeTree)(given ctx: Context): Type

  /** Type tree representing an inferred type */
  type Inferred <: TypeTree

  def matchInferred(tree: Tree)(given ctx: Context): Option[Inferred]

  def Inferred_apply(tpe: Type)(given ctx: Context): Inferred

  /** Type tree representing a reference to definition with a given name */
  type TypeIdent <: TypeTree

  def matchTypeIdent(tree: Tree)(given ctx: Context): Option[TypeIdent]

  def TypeIdent_name(self: TypeIdent)(given ctx: Context): String

  def TypeIdent_copy(original: TypeIdent)(name: String)(given ctx: Context): TypeIdent

  /** Type tree representing a selection of definition with a given name on a given term prefix */
  type TypeSelect <: TypeTree

  def matchTypeSelect(tree: Tree)(given ctx: Context): Option[TypeSelect]

  def TypeSelect_qualifier(self: TypeSelect)(given ctx: Context): Term
  def TypeSelect_name(self: TypeSelect)(given ctx: Context): String

  def TypeSelect_apply(qualifier: Term, name: String)(given ctx: Context): TypeSelect
  def TypeSelect_copy(original: TypeSelect)(qualifier: Term, name: String)(given ctx: Context): TypeSelect

  /** Type tree representing a selection of definition with a given name on a given type prefix */
  type Projection <: TypeTree

  def matchProjection(tree: Tree)(given ctx: Context): Option[Projection]

  def Projection_qualifier(self: Projection)(given ctx: Context): TypeTree
  def Projection_name(self: Projection)(given ctx: Context): String

  def Projection_copy(original: Projection)(qualifier: TypeTree, name: String)(given ctx: Context): Projection

  /** Type tree representing a singleton type */
  type Singleton <: TypeTree

  def matchSingleton(tree: Tree)(given ctx: Context): Option[Singleton]

  def Singleton_ref(self: Singleton)(given ctx: Context): Term

  def Singleton_apply(ref: Term)(given ctx: Context): Singleton
  def Singleton_copy(original: Singleton)(ref: Term)(given ctx: Context): Singleton

  /** Type tree representing a type refinement */
  type Refined <: TypeTree

  def matchRefined(tree: Tree)(given ctx: Context): Option[Refined]

  def Refined_tpt(self: Refined)(given ctx: Context): TypeTree
  def Refined_refinements(self: Refined)(given ctx: Context): List[Definition]

  def Refined_copy(original: Refined)(tpt: TypeTree, refinements: List[Definition])(given ctx: Context): Refined

  /** Type tree representing a type application */
  type Applied <: TypeTree

  def matchApplied(tree: Tree)(given ctx: Context): Option[Applied]

  def Applied_tpt(self: Applied)(given ctx: Context): TypeTree
  def Applied_args(self: Applied)(given ctx: Context): List[Tree /*TypeTree | TypeBoundsTree*/]

  def Applied_apply(tpt: TypeTree, args: List[Tree /*TypeTree | TypeBoundsTree*/])(given ctx: Context): Applied
  def Applied_copy(original: Applied)(tpt: TypeTree, args: List[Tree /*TypeTree | TypeBoundsTree*/])(given ctx: Context): Applied

  /** Type tree representing an annotated type */
  type Annotated <: TypeTree

  def matchAnnotated(tree: Tree)(given ctx: Context): Option[Annotated]

  def Annotated_arg(self: Annotated)(given ctx: Context): TypeTree
  def Annotated_annotation(self: Annotated)(given ctx: Context): Term

  def Annotated_apply(arg: TypeTree, annotation: Term)(given ctx: Context): Annotated
  def Annotated_copy(original: Annotated)(arg: TypeTree, annotation: Term)(given ctx: Context): Annotated

  /** Type tree representing a type match */
  type MatchTypeTree <: TypeTree

  def matchMatchTypeTree(tree: Tree)(given ctx: Context): Option[MatchTypeTree]

  def MatchTypeTree_bound(self: MatchTypeTree)(given ctx: Context): Option[TypeTree]
  def MatchTypeTree_selector(self: MatchTypeTree)(given ctx: Context): TypeTree
  def MatchTypeTree_cases(self: MatchTypeTree)(given ctx: Context): List[TypeCaseDef]

  def MatchTypeTree_apply(bound: Option[TypeTree], selector: TypeTree, cases: List[TypeCaseDef])(given ctx: Context): MatchTypeTree
  def MatchTypeTree_copy(original: MatchTypeTree)(bound: Option[TypeTree], selector: TypeTree, cases: List[TypeCaseDef])(given ctx: Context): MatchTypeTree

  /** Type tree representing a by name parameter */
  type ByName <: TypeTree

  def ByName_result(self: ByName)(given ctx: Context): TypeTree

  def matchByName(tree: Tree)(given ctx: Context): Option[ByName]

  def ByName_apply(result: TypeTree)(given ctx: Context): ByName
  def ByName_copy(original: ByName)(result: TypeTree)(given ctx: Context): ByName

  /** Type tree representing a lambda abstraction type */
  type LambdaTypeTree <: TypeTree

  def matchLambdaTypeTree(tree: Tree)(given ctx: Context): Option[LambdaTypeTree]

  def Lambdatparams(self: LambdaTypeTree)(given ctx: Context): List[TypeDef]
  def Lambdabody(self: LambdaTypeTree)(given ctx: Context): Tree /*TypeTree | TypeBoundsTree*/

  def Lambdaapply(tparams: List[TypeDef], body: Tree /*TypeTree | TypeBoundsTree*/)(given ctx: Context): LambdaTypeTree
  def Lambdacopy(original: LambdaTypeTree)(tparams: List[TypeDef], body: Tree /*TypeTree | TypeBoundsTree*/)(given ctx: Context): LambdaTypeTree

  /** Type tree representing a type binding */
  type TypeBind <: TypeTree

  def matchTypeBind(tree: Tree)(given ctx: Context): Option[TypeBind]

  def TypeBind_name(self: TypeBind)(given ctx: Context): String
  def TypeBind_body(self: TypeBind)(given ctx: Context): Tree /*TypeTree | TypeBoundsTree*/

  def TypeBind_copy(original: TypeBind)(name: String, tpt: Tree /*TypeTree | TypeBoundsTree*/)(given ctx: Context): TypeBind

  /** Type tree within a block with aliases `{ type U1 = ... ; T[U1, U2] }` */
  type TypeBlock <: TypeTree

  def matchTypeBlock(tree: Tree)(given ctx: Context): Option[TypeBlock]

  def TypeBlock_aliases(self: TypeBlock)(given ctx: Context): List[TypeDef]
  def TypeBlock_tpt(self: TypeBlock)(given ctx: Context): TypeTree

  def TypeBlock_apply(aliases: List[TypeDef], tpt: TypeTree)(given ctx: Context): TypeBlock
  def TypeBlock_copy(original: TypeBlock)(aliases: List[TypeDef], tpt: TypeTree)(given ctx: Context): TypeBlock

  /** Type tree representing a type bound written in the source */
  type TypeBoundsTree <: Tree /*TypeTree | TypeBoundsTree*/

  def matchTypeBoundsTree(tree: Tree)(given ctx: Context): Option[TypeBoundsTree]

  def TypeBoundsTree_tpe(self: TypeBoundsTree)(given ctx: Context): TypeBounds
  def TypeBoundsTree_low(self: TypeBoundsTree)(given ctx: Context): TypeTree
  def TypeBoundsTree_hi(self: TypeBoundsTree)(given ctx: Context): TypeTree

  /** Type tree representing wildcard type bounds written in the source.
    *  The wildcard type `_` (for example in in `List[_]`) will be a type tree that
    *  represents a type but has `TypeBound`a inside.
    */
  type WildcardTypeTree <: Tree

  def matchWildcardTypeTree(tree: Tree)(given ctx: Context): Option[WildcardTypeTree]

  def WildcardTypeTree_tpe(self: WildcardTypeTree)(given ctx: Context): TypeOrBounds

  /** Branch of a pattern match or catch clause */
  type CaseDef <: Tree

  def matchCaseDef(tree: Tree)(given ctx: Context): Option[CaseDef]

  def CaseDef_pattern(self: CaseDef)(given ctx: Context): Tree
  def CaseDef_guard(self: CaseDef)(given ctx: Context): Option[Term]
  def CaseDef_rhs(self: CaseDef)(given ctx: Context): Term

  def CaseDef_module_apply(pattern: Tree, guard: Option[Term], body: Term)(given ctx: Context): CaseDef
  def CaseDef_module_copy(original: CaseDef)(pattern: Tree, guard: Option[Term], body: Term)(given ctx: Context): CaseDef

  /** Branch of a type pattern match */
  type TypeCaseDef <: Tree

  def matchTypeCaseDef(tree: Tree)(given ctx: Context): Option[TypeCaseDef]

  def TypeCaseDef_pattern(self: TypeCaseDef)(given ctx: Context): TypeTree
  def TypeCaseDef_rhs(self: TypeCaseDef)(given ctx: Context): TypeTree

  def TypeCaseDef_module_apply(pattern: TypeTree, body: TypeTree)(given ctx: Context): TypeCaseDef
  def TypeCaseDef_module_copy(original: TypeCaseDef)(pattern: TypeTree, body: TypeTree)(given ctx: Context): TypeCaseDef

  //
  // PATTERNS
  //

  /** Tree representing a binding pattern `_ @ _` */
  type Bind <: Tree

  def matchTree_Bind(x: Tree)(given ctx: Context): Option[Bind]

  def Tree_Bind_name(self: Bind)(given ctx: Context): String

  def Tree_Bind_pattern(self: Bind)(given ctx: Context): Tree

  def Tree_Bind_module_copy(original: Bind)(name: String, pattern: Tree)(given ctx: Context): Bind

  /** Tree representing an unapply pattern `Xyz(...)` */
  type Unapply <: Tree

  def matchTree_Unapply(pattern: Tree)(given ctx: Context): Option[Unapply]

  def Tree_Unapply_fun(self: Unapply)(given ctx: Context): Term

  def Tree_Unapply_implicits(self: Unapply)(given ctx: Context): List[Term]

  def Tree_Unapply_patterns(self: Unapply)(given ctx: Context): List[Tree]

  def Tree_Unapply_module_copy(original: Unapply)(fun: Term, implicits: List[Term], patterns: List[Tree])(given ctx: Context): Unapply

  /** Tree representing pattern alternatives `X | Y | ...` */
  type Alternatives <: Tree

  def matchTree_Alternatives(pattern: Tree)(given ctx: Context): Option[Alternatives]

  def Tree_Alternatives_patterns(self: Alternatives)(given ctx: Context): List[Tree]

  def Tree_Alternatives_module_apply(patterns: List[Tree])(given ctx: Context): Alternatives
  def Tree_Alternatives_module_copy(original: Alternatives)(patterns: List[Tree])(given ctx: Context): Alternatives


  //
  // TYPES
  //

  /** Type or bounds */
  type TypeOrBounds <: AnyRef

  /** NoPrefix for a type selection */
  type NoPrefix <: TypeOrBounds

  def matchNoPrefix(x: TypeOrBounds)(given ctx: Context): Option[NoPrefix]

  /** Type bounds */
  type TypeBounds <: TypeOrBounds

  def matchTypeBounds(x: TypeOrBounds)(given ctx: Context): Option[TypeBounds]

  def TypeBounds_low(self: TypeBounds)(given ctx: Context): Type
  def TypeBounds_hi(self: TypeBounds)(given ctx: Context): Type

  /** A type */
  type Type <: TypeOrBounds

  def matchType(x: TypeOrBounds)(given ctx: Context): Option[Type]

  def Type_apply(clazz: Class[_])(given ctx: Context): Type

  def `Type_=:=`(self: Type)(that: Type)(given ctx: Context): Boolean
  def `Type_<:<`(self: Type)(that: Type)(given ctx: Context): Boolean

  /** Widen from singleton type to its underlying non-singleton
    *  base type by applying one or more `underlying` dereferences,
    *  Also go from => T to T.
    *  Identity for all other types. Example:
    *
    *  class Outer { class C ; val x: C }
    *  def o: Outer
    *  <o.x.type>.widen = o.C
    */
  def Type_widen(self: Type)(given ctx: Context): Type

  /** Follow aliases and dereferences LazyRefs, annotated types and instantiated
   *  TypeVars until type is no longer alias type, annotated type, LazyRef,
   *  or instantiated type variable.
   */
  def Type_dealias(self: Type)(given ctx: Context): Type

  def Type_simplified(self: Type)(given ctx: Context): Type

  def Type_classSymbol(self: Type)(given ctx: Context): Option[Symbol] // TODO remove Option and use NoSymbol

  def Type_typeSymbol(self: Type)(given ctx: Context): Symbol

  def Type_termSymbol(self: Type)(given ctx: Context): Symbol

  def Type_isSingleton(self: Type)(given ctx: Context): Boolean

  def Type_memberType(self: Type)(member: Symbol)(given ctx: Context): Type

  /** Is this type an instance of a non-bottom subclass of the given class `cls`? */
  def Type_derivesFrom(self: Type)(cls: Symbol)(given ctx: Context): Boolean

  /** Is this type a function type?
   *
   *  @return true if the dealised type of `self` without refinement is `FunctionN[T1, T2, ..., Tn]`
   *
   *  @note The function
   *
   *     - returns true for `given Int => Int` and `erased Int => Int`
   *     - returns false for `List[Int]`, despite that `List[Int] <:< Int => Int`.
   */
  def Type_isFunctionType(self: Type)(given ctx: Context): Boolean


  /** Is this type an implicit function type?
   *
   *  @see `Type_isFunctionType`
   */
  def Type_isImplicitFunctionType(self: Type)(given ctx: Context): Boolean

  /** Is this type an erased function type?
   *
   *  @see `Type_isFunctionType`
   */
  def Type_isErasedFunctionType(self: Type)(given ctx: Context): Boolean

  /** Is this type a dependent function type?
   *
   *  @see `Type_isFunctionType`
   */
  def Type_isDependentFunctionType(self: Type)(given ctx: Context): Boolean

  /** A singleton type representing a known constant value */
  type ConstantType <: Type

  def matchConstantType(tpe: TypeOrBounds)(given ctx: Context): Option[ConstantType]

  def ConstantType_constant(self: ConstantType)(given ctx: Context): Constant

  /** Type of a reference to a term symbol */
  type TermRef <: Type

  def matchTermRef(tpe: TypeOrBounds)(given ctx: Context): Option[TermRef]

  def TermRef_apply(qual: TypeOrBounds, name: String)(given ctx: Context): TermRef

  def TermRef_qualifier(self: TermRef)(given ctx: Context): TypeOrBounds
  def TermRef_name(self: TermRef)(given ctx: Context): String

  /** Type of a reference to a type symbol */
  type TypeRef <: Type

  def matchTypeRef(tpe: TypeOrBounds)(given ctx: Context): Option[TypeRef]

  def TypeRef_qualifier(self: TypeRef)(given ctx: Context): TypeOrBounds
  def TypeRef_name(self: TypeRef)(given Context): String

  /** Type of a `super` refernce */
  type SuperType <: Type

  def matchSuperType(tpe: TypeOrBounds)(given ctx: Context): Option[SuperType]

  def SuperType_thistpe(self: SuperType)(given ctx: Context): Type
  def SuperType_supertpe(self: SuperType)(given ctx: Context): Type

  /** A type with a type refinement `T { type U }` */
  type Refinement <: Type

  def matchRefinement(tpe: TypeOrBounds)(given ctx: Context): Option[Refinement]

  def Refinement_parent(self: Refinement)(given ctx: Context): Type
  def Refinement_name(self: Refinement)(given ctx: Context): String
  def Refinement_info(self: Refinement)(given ctx: Context): TypeOrBounds

  /** A higher kinded type applied to some types `T[U]` */
  type AppliedType <: Type

  def matchAppliedType(tpe: TypeOrBounds)(given ctx: Context): Option[AppliedType]

  def AppliedType_tycon(self: AppliedType)(given ctx: Context): Type
  def AppliedType_args(self: AppliedType)(given ctx: Context): List[TypeOrBounds]

  def AppliedType_apply(tycon: Type, args: List[TypeOrBounds])(given ctx: Context) : AppliedType

  /** A type with an anottation `T @foo` */
  type AnnotatedType <: Type

  def matchAnnotatedType(tpe: TypeOrBounds)(given ctx: Context): Option[AnnotatedType]

  def AnnotatedType_underlying(self: AnnotatedType)(given ctx: Context): Type
  def AnnotatedType_annot(self: AnnotatedType)(given ctx: Context): Term

  /** Intersection type `T & U` */
  type AndType <: Type

  def matchAndType(tpe: TypeOrBounds)(given ctx: Context): Option[AndType]

  def AndType_left(self: AndType)(given ctx: Context): Type
  def AndType_right(self: AndType)(given ctx: Context): Type

  /** Union type `T | U` */
  type OrType <: Type

  def matchOrType(tpe: TypeOrBounds)(given ctx: Context): Option[OrType]

  def OrType_left(self: OrType)(given ctx: Context): Type
  def OrType_right(self: OrType)(given ctx: Context): Type

  /** Type match `T match { case U => ... }` */
  type MatchType <: Type

  def matchMatchType(tpe: TypeOrBounds)(given ctx: Context): Option[MatchType]

  def MatchType_bound(self: MatchType)(given ctx: Context): Type
  def MatchType_scrutinee(self: MatchType)(given ctx: Context): Type
  def MatchType_cases(self: MatchType)(given ctx: Context): List[Type]

  /** Type of a by by name parameter */
  type ByNameType <: Type

  def matchByNameType(tpe: TypeOrBounds)(given ctx: Context): Option[ByNameType]

  def ByNameType_underlying(self: ByNameType)(given ctx: Context): Type

  /** Type of a parameter reference */
  type ParamRef <: Type

  def matchParamRef(tpe: TypeOrBounds)(given ctx: Context): Option[ParamRef]

  def ParamRef_binder(self: ParamRef)(given ctx: Context): LambdaType[TypeOrBounds]
  def ParamRef_paramNum(self: ParamRef)(given ctx: Context): Int

  /** Type of `this` */
  type ThisType <: Type

  def matchThisType(tpe: TypeOrBounds)(given ctx: Context): Option[ThisType]

  def ThisType_tref(self: ThisType)(given ctx: Context): Type

  /** A type that is recursively defined `this` */
  type RecursiveThis <: Type

  def matchRecursiveThis(tpe: TypeOrBounds)(given ctx: Context): Option[RecursiveThis]

  def RecursiveThis_binder(self: RecursiveThis)(given ctx: Context): RecursiveType

  /** A type that is recursively defined */
  type RecursiveType <: Type

  def matchRecursiveType(tpe: TypeOrBounds)(given ctx: Context): Option[RecursiveType]

  def RecursiveType_underlying(self: RecursiveType)(given ctx: Context): Type

  // TODO can we add the bound back without an cake?
  // TODO is LambdaType really needed? ParamRefExtractor could be split into more precise extractors
  /** Common abstraction for lambda types (MethodType, PolyType and TypeLambda). */
  type LambdaType[ParamInfo /*<: TypeOrBounds*/] <: Type

  /** Type of the definition of a method taking a single list of parameters. It's return type may be a MethodType. */
  type MethodType <: LambdaType[Type]

  def matchMethodType(tpe: TypeOrBounds)(given ctx: Context): Option[MethodType]

  def MethodType_isErased(self: MethodType): Boolean
  def MethodType_isImplicit(self: MethodType): Boolean
  def MethodType_paramNames(self: MethodType)(given ctx: Context): List[String]
  def MethodType_paramTypes(self: MethodType)(given ctx: Context): List[Type]
  def MethodType_resType(self: MethodType)(given ctx: Context): Type

  /** Type of the definition of a method taking a list of type parameters. It's return type may be a MethodType. */
  type PolyType <: LambdaType[TypeBounds]

  def matchPolyType(tpe: TypeOrBounds)(given ctx: Context): Option[PolyType]

  def PolyType_paramNames(self: PolyType)(given ctx: Context): List[String]
  def PolyType_paramBounds(self: PolyType)(given ctx: Context): List[TypeBounds]
  def PolyType_resType(self: PolyType)(given ctx: Context): Type

  /** Type of the definition of a type lambda taking a list of type parameters. It's return type may be a TypeLambda. */
  type TypeLambda <: LambdaType[TypeBounds]

  def matchTypeLambda(tpe: TypeOrBounds)(given ctx: Context): Option[TypeLambda]

  def TypeLambda_paramNames(self: TypeLambda)(given ctx: Context): List[String]
  def TypeLambda_paramBounds(self: TypeLambda)(given ctx: Context): List[TypeBounds]
  def TypeLambda_resType(self: TypeLambda)(given ctx: Context): Type

  //
  // IMPORT SELECTORS
  //

  /** Import selectors:
   *  * SimpleSelector: `.bar` in `import foo.bar`
   *  * RenameSelector: `.{bar => baz}` in `import foo.{bar => baz}`
   *  * OmitSelector: `.{bar => _}` in `import foo.{bar => _}`
   */
  type ImportSelector <: AnyRef

  type SimpleSelector <: ImportSelector

  def matchSimpleSelector(self: ImportSelector)(given ctx: Context): Option[SimpleSelector]

  def SimpleSelector_selection(self: SimpleSelector)(given ctx: Context): Id

  type RenameSelector <: ImportSelector

  def matchRenameSelector(self: ImportSelector)(given ctx: Context): Option[RenameSelector]

  def RenameSelector_from(self: RenameSelector)(given ctx: Context): Id
  def RenameSelector_to(self: RenameSelector)(given ctx: Context): Id

  type OmitSelector <: ImportSelector

  def matchOmitSelector(self: ImportSelector)(given ctx: Context): Option[OmitSelector]

  def SimpleSelector_omitted(self: OmitSelector)(given ctx: Context): Id

  //
  // IDENTIFIERS
  //

  /** Untyped identifier */
  type Id <: AnyRef

  /** Position in the source code */
  def Id_pos(self: Id)(given ctx: Context): Position

  /** Name of the identifier */
  def Id_name(self: Id)(given ctx: Context): String

  //
  // SIGNATURES
  //

  type Signature <: AnyRef

  def Signature_paramSigs(self: Signature): List[String | Int]

  def Signature_resultSig(self: Signature): String

  //
  // POSITIONS
  //

  /** Position in a source file */
  type Position <: AnyRef

  /** The start offset in the source file */
  def Position_start(self: Position): Int

  /** The end offset in the source file */
  def Position_end(self: Position): Int

  /** Does this position exist */
  def Position_exists(self: Position): Boolean

  /** Source file in which this position is located */
  def Position_sourceFile(self: Position): SourceFile

  /** The start line in the source file */
  def Position_startLine(self: Position): Int

  /** The end line in the source file */
  def Position_endLine(self: Position): Int

  /** The start column in the source file */
  def Position_startColumn(self: Position): Int

  /** The end column in the source file */
  def Position_endColumn(self: Position): Int

  /** Source code within the position */
  def Position_sourceCode(self: Position): String

  //
  // SOURCE FILE
  //

  /** Scala source file */
  type SourceFile <: AnyRef

  /** Path to a source file */
  def SourceFile_jpath(self: SourceFile): java.nio.file.Path

  /** Content of a source file */
  def SourceFile_content(self: SourceFile): String

  //
  // COMMENTS
  //

  /** Comment */
  type Comment <: AnyRef

  def Comment_raw(self: Comment): String
  def Comment_expanded(self: Comment): Option[String]
  def Comment_usecases(self: Comment): List[(String, Option[DefDef])]

  //
  // CONSTANTS
  //

  /** Constant value represented as the constant itself */
  type Constant <: AnyRef

  def Constant_value(const: Constant): Any

  def matchConstant(constant: Constant): Option[Unit | Null | Int | Boolean | Byte | Short | Int | Long | Float | Double | Char | String | Type]
  def matchConstant_ClassTag(constant: Constant): Option[Type]

  def Constant_apply(x: Unit | Null | Int | Boolean | Byte | Short | Int | Long | Float | Double | Char | String | Type): Constant
  def Constant_ClassTag_apply(x: Type): Constant

  //
  // SYMBOLS
  //

  /** Symbol of a definition.
   *  Then can be compared with == to know if the definition is the same.
   */
  type Symbol <: AnyRef

  /** Owner of this symbol. The owner is the symbol in which this symbol is defined. */
  def Symbol_owner(self: Symbol)(given ctx: Context): Symbol

  /** Flags of this symbol */
  def Symbol_flags(self: Symbol)(given ctx: Context): Flags

  def Symbol_tree(self: Symbol)(given ctx: Context): Tree

  def Symbol_isLocalDummy(self: Symbol)(given ctx: Context): Boolean

  def Symbol_isRefinementClass(self: Symbol)(given ctx: Context): Boolean

  def Symbol_isAliasType(self: Symbol)(given ctx: Context): Boolean

  def Symbol_isAnonymousClass(self: Symbol)(given ctx: Context): Boolean

  def Symbol_isAnonymousFunction(self: Symbol)(given ctx: Context): Boolean

  def Symbol_isAbstractType(self: Symbol)(given ctx: Context): Boolean

  def Symbol_isClassConstructor(self: Symbol)(given ctx: Context): Boolean

  /** This symbol is private within the resulting type. */
  def Symbol_privateWithin(self: Symbol)(given ctx: Context): Option[Type]

  /** This symbol is protected within the resulting type. */
  def Symbol_protectedWithin(self: Symbol)(given ctx: Context): Option[Type]

  /** The name of this symbol. */
  def Symbol_name(self: Symbol)(given ctx: Context): String

  /** The full name of this symbol up to the root package. */
  def Symbol_fullName(self: Symbol)(given ctx: Context): String

  /** The position of this symbol */
  def Symbol_pos(self: Symbol)(given ctx: Context): Position

  def Symbol_localContext(self: Symbol)(given ctx: Context): Context

  /** The comment of the symbol */
  def Symbol_comment(self: Symbol)(given ctx: Context): Option[Comment]

  /** Annotations attached to this symbol */
  def Symbol_annots(self: Symbol)(given ctx: Context): List[Term]

  def Symbol_isDefinedInCurrentRun(self: Symbol)(given ctx: Context): Boolean

  /** Fields directly declared in the class */
  def Symbol_fields(self: Symbol)(given ctx: Context): List[Symbol]

  /** Field with the given name directly declared in the class */
  def Symbol_field(self: Symbol)(name: String)(given ctx: Context): Symbol

  /** Get non-private named methods defined directly inside the class */
  def Symbol_classMethod(self: Symbol)(name: String)(given ctx: Context): List[Symbol]

  /** Get all non-private methods defined directly inside the class, excluding constructors */
  def Symbol_classMethods(self: Symbol)(given ctx: Context): List[Symbol]

  /** Get named non-private methods declared or inherited */
  def Symbol_method(self: Symbol)(name: String)(given ctx: Context): List[Symbol]

  /** Get all non-private methods declared or inherited */
  def Symbol_methods(self: Symbol)(given ctx: Context): List[Symbol]

  /** Fields of a case class type -- only the ones declared in primary constructor */
  def Symbol_caseFields(self: Symbol)(given ctx: Context): List[Symbol]

  def Symbol_of(fullName: String)(given ctx: Context): Symbol

  def Symbol_isTypeParam(self: Symbol)(given ctx: Context): Boolean

  def Symbol_isPackageDef(symbol: Symbol)(given ctx: Context): Boolean

  /** Is this the definition of a type? */
  def Symbol_isType(symbol: Symbol)(given ctx: Context): Boolean

  /** Is this the definition of a term? */
  def Symbol_isTerm(symbol: Symbol)(given ctx: Context): Boolean

  /** Is this the definition of a ClassDef tree? */
  def Symbol_isClassDef(symbol: Symbol)(given ctx: Context): Boolean

  /** Is this the definition of a TypeDef tree? */
  def Symbol_isTypeDef(symbol: Symbol)(given ctx: Context): Boolean

  /** Is this the definition of a DefDef tree? */
  def Symbol_isDefDef(symbol: Symbol)(given ctx: Context): Boolean

  /** Is this the definition of a ValDef tree? */
  def Symbol_isValDef(symbol: Symbol)(given ctx: Context): Boolean

  /** Is this the definition of a Bind pattern? */
  def Symbol_isBind(symbol: Symbol)(given ctx: Context): Boolean

  /** Signature of this definition */
  def Symbol_signature(self: Symbol)(given ctx: Context): Signature

  /** The class symbol of the companion module class */
  def Symbol_moduleClass(self: Symbol)(given ctx: Context): Symbol

  /** The symbol of the companion class */
  def Symbol_companionClass(self: Symbol)(given ctx: Context): Symbol

  /** The symbol of the companion module */
  def Symbol_companionModule(self: Symbol)(given ctx: Context): Symbol

  def Symbol_noSymbol(given ctx: Context): Symbol

  //
  // FLAGS
  //

  /** FlagSet of a Symbol */
  type Flags

  /** Is the given flag set a subset of this flag sets */
  def Flags_is(self: Flags)(that: Flags): Boolean

  /** Union of the two flag sets */
  def Flags_or(self: Flags)(that: Flags): Flags

  /** Intersection of the two flag sets */
  def Flags_and(self: Flags)(that: Flags): Flags

  def Flags_EmptyFlags: Flags
  def Flags_Private: Flags
  def Flags_Protected: Flags
  def Flags_Abstract: Flags
  def Flags_Final: Flags
  def Flags_Sealed: Flags
  def Flags_Case: Flags
  def Flags_Implicit: Flags
  def Flags_Given: Flags
  def Flags_Erased: Flags
  def Flags_Lazy: Flags
  def Flags_Override: Flags
  def Flags_Inline: Flags
  def Flags_Macro: Flags
  def Flags_Static: Flags
  def Flags_JavaDefined: Flags
  def Flags_Object: Flags
  def Flags_Trait: Flags
  def Flags_Local: Flags
  def Flags_Synthetic: Flags
  def Flags_Artifact: Flags
  def Flags_Mutable: Flags
  def Flags_FieldAccessor: Flags
  def Flags_CaseAcessor: Flags
  def Flags_Covariant: Flags
  def Flags_Contravariant: Flags
  def Flags_Scala2X: Flags
  def Flags_DefaultParameterized: Flags
  def Flags_StableRealizable: Flags
  def Flags_Param: Flags
  def Flags_ParamAccessor: Flags
  def Flags_Enum: Flags
  def Flags_ModuleClass: Flags
  def Flags_PrivateLocal: Flags
  def Flags_Package: Flags

  //
  // QUOTED SEAL/UNSEAL
  //

  /** View this expression `quoted.Expr[_]` as a `Term` */
  def QuotedExpr_unseal(self: scala.quoted.Expr[_])(given ctx: Context): Term

  /** Checked cast to a `quoted.Expr[U]` */
  def QuotedExpr_cast[U](self: scala.quoted.Expr[_])(given tp: scala.quoted.Type[U], ctx: Context): scala.quoted.Expr[U]

  /** View this expression `quoted.Type[T]` as a `TypeTree` */
  def QuotedType_unseal(self: scala.quoted.Type[_])(given ctx: Context): TypeTree

  /** Convert `Term` to an `quoted.Expr[Any]` */
  def QuotedExpr_seal(self: Term)(given ctx: Context): scala.quoted.Expr[Any]


  /** Convert `Type` to an `quoted.Type[_]` */
  def QuotedType_seal(self: Type)(given ctx: Context): scala.quoted.Type[_]

  //
  // DEFINITIONS
  //

  def Definitions_RootPackage: Symbol
  def Definitions_RootClass: Symbol

  def Definitions_EmptyPackageClass: Symbol

  def Definitions_ScalaPackage: Symbol
  def Definitions_ScalaPackageClass: Symbol

  def Definitions_AnyClass: Symbol
  def Definitions_AnyValClass: Symbol
  def Definitions_ObjectClass: Symbol
  def Definitions_AnyRefClass: Symbol
  def Definitions_NullClass: Symbol
  def Definitions_NothingClass: Symbol
  def Definitions_UnitClass: Symbol
  def Definitions_ByteClass: Symbol
  def Definitions_ShortClass: Symbol
  def Definitions_CharClass: Symbol
  def Definitions_IntClass: Symbol
  def Definitions_LongClass: Symbol
  def Definitions_FloatClass: Symbol
  def Definitions_DoubleClass: Symbol
  def Definitions_BooleanClass: Symbol
  def Definitions_StringClass: Symbol
  def Definitions_ClassClass: Symbol
  def Definitions_ArrayClass: Symbol
  def Definitions_PredefModule: Symbol
  def Definitions_Predef_classOf: Symbol

  def Definitions_JavaLangPackage: Symbol

  def Definitions_ArrayModule: Symbol

  def Definitions_Array_apply: Symbol
  def Definitions_Array_clone: Symbol
  def Definitions_Array_length: Symbol
  def Definitions_Array_update: Symbol

  def Definitions_RepeatedParamClass: Symbol

  def Definitions_OptionClass: Symbol
  def Definitions_NoneModule: Symbol
  def Definitions_SomeModule: Symbol

  def Definitions_ProductClass: Symbol
  // TODO avoid default parameters
  def Definitions_FunctionClass(arity: Int, isImplicit: Boolean = false, isErased: Boolean = false): Symbol

  def Definitions_TupleClass(arity: Int): Symbol

  /** Symbol of scala.internal.Quoted.patternHole */
  def Definitions_InternalQuoted_patternHole: Symbol

  /** Symbol of scala.internal.Quoted.patternBindHole */
  def Definitions_InternalQuoted_patternBindHoleAnnot: Symbol

  /** Symbol of scala.internal.Quoted.fromAbove */
  def Definitions_InternalQuoted_fromAboveAnnot: Symbol

  def Definitions_UnitType: Type
  def Definitions_ByteType: Type
  def Definitions_ShortType: Type
  def Definitions_CharType: Type
  def Definitions_IntType: Type
  def Definitions_LongType: Type
  def Definitions_FloatType: Type
  def Definitions_DoubleType: Type
  def Definitions_BooleanType: Type
  def Definitions_AnyType: Type
  def Definitions_AnyValType: Type
  def Definitions_AnyRefType: Type
  def Definitions_ObjectType: Type
  def Definitions_NothingType: Type
  def Definitions_NullType: Type
  def Definitions_StringType: Type

  //
  // IMPLICITS
  //

  type ImplicitSearchResult <: AnyRef

  type ImplicitSearchSuccess <: ImplicitSearchResult
  def matchImplicitSearchSuccess(isr: ImplicitSearchResult)(given ctx: Context): Option[ImplicitSearchSuccess]
  def ImplicitSearchSuccess_tree(self: ImplicitSearchSuccess)(given ctx: Context): Term

  type ImplicitSearchFailure <: ImplicitSearchResult
  def matchImplicitSearchFailure(isr: ImplicitSearchResult)(given ctx: Context): Option[ImplicitSearchFailure]
  def ImplicitSearchFailure_explanation(self: ImplicitSearchFailure)(given ctx: Context): String

  type DivergingImplicit <: ImplicitSearchFailure
  def matchDivergingImplicit(isr: ImplicitSearchResult)(given ctx: Context): Option[DivergingImplicit]

  type NoMatchingImplicits <: ImplicitSearchFailure
  def matchNoMatchingImplicits(isr: ImplicitSearchResult)(given ctx: Context): Option[NoMatchingImplicits]

  type AmbiguousImplicits <: ImplicitSearchFailure
  def matchAmbiguousImplicits(isr: ImplicitSearchResult)(given ctx: Context): Option[AmbiguousImplicits]

  /** Find an implicit of type `T` in the current scope given by `ctx`.
   *  Return an `ImplicitSearchResult`.
   *
   *  @param tpe type of the implicit parameter
   *  @param ctx current context
   */
  def searchImplicit(tpe: Type)(given ctx: Context): ImplicitSearchResult

  /** Inline fn if it is an explicit closure possibly nested inside the expression of a block.
   *  Otherwise apply the arguments to the closure.
   */
  def betaReduce(f: Term, args: List[Term])(given ctx: Context): Term

}
