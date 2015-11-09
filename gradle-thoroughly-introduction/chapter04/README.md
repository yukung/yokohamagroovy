# Chapter 4 Java プロジェクトのビルド

## Java プラグインとは

- タスク
- 規約
- プロパティ
- ソースセット

の4つの要素で構成されているコンポーネント。

## ソースセット

コンパイル対象の Java ソースコード及びリソースの論理的な集合。デフォルトで `main` と `test` の2つが定義されている。自由に追加できる。

また、以下の概念を包含する。

- ソースセットごとに専用のタスクが提供される
	- `compileJava`, `processResources`, `compileTestJava`, `processTestResources` など
	- それぞれのタスクにもタスク用のプロパティが提供される
		- `JavaCompile` 型のタスク（`compileJava` など）のプロパティなら `classpath`, `destinationDir`, `includes`, `excludes`, `options` など
		- `Copy` 型のタスク（`processResources` など）のプロパティなら `destinationDir`, `duplicatesStrategy`, `includes`, `excludes`, `includeEmptyDirs` など
		- 他にも `Jar` 型のタスクや `Javadoc` 型のタスクなどがあり、それぞれプロパティが異なる
- ソースセットごとに専用のプロパティが提供される
	- `output`, `output.classDir`, `output.resourcesDir`, `compileClasspath`, `runtimeClasspath` など

ソースセットごとのプロパティには、`Project` オブジェクトの `sourceSets` プロパティを経由してアクセスできる。

```groovy
println sourceSets.test.compileClasspath
println sourceSets['test'].compileClasspath
```

## プロパティ

多くのプロパティにはデフォルト値が設定され、Gradle の規約に沿うように自動的に設定される。規約に沿わない場合は明示的に指定することで挙動を変えることができる。

Java プラグインが提供するプロパティには以下の様なもの（一部）がある。

| プロパティ | 説明 | デフォルト値 |
| ---------- | ---- | ------------ |
| `testResultDir` | テスト結果の XML ファイル出力先 | `build/test-results` |
| `testReportDir` | テストレポート（HTML）出力先 | `build/reports/tests` |
| `libsDir` | ライブラリ（JAR ファイル）出力先 | `build/libs` |
| `distDir` | ディストリビューション（ZIP, TAR）の出力先 | `build/distributions` |
| `docsDir` | ドキュメント（Javadoc）出力先 | `build/docs` |
| `sourceSets` | プロジェクトのソースセット | `main` および `test` ソースセットのコンテナ |
| `sourceCompatibility` | Java ソースのコンパイル時に指定する Java バージョン | ビルドスクリプト実行環境の JavaVM バージョン |
| `targetCompatibility` | コンパイル時のクラス生成のターゲットバージョン | `sourceCompatibility` プロパティの値 |
| `archivesBaseName` | JAR などのアーカイブファイルのベースネーム | `project.name` プロパティの値（デフォルトではプロジェクトのルートディレクトリ名） |
| `manifest` | すべての JAR ファイルに含めるマニフェスト | なし |

これらのプロパティは `Project` オブジェクトのプロパティとしてアクセスできる。

```groovy
println project.docsDir
println docsDir
```

## タスク

Gradle で行う処理の単位。プラグインによって自動的に定義され利用できるようになる。実行する場合はコマンドラインからタスク名を指定して実行する。Java プラグインであれば、

- `compileJava`
- `processResources`
- `classes`
- `compileTestJava`
- `processTestResources`
- `testClasses`
- `jar`
- `javadoc`
- `test`
- `uploadArchives`
- `clean`
- `clean<TaskName>`
- `assemble`
- `check`
- `build`
- `buildNeeded`
- `buildDependents`

などが提供される。タスク間には依存関係が有り、その依存関係に基づいて実行順が決定される。

また、Gradle はビルド内で同じタスクが複数実行されないように保証されている。

## 規約

プラグインが提供するビルドに関するルールや決まり。これに沿うことでビルドスクリプトがデフォルト値で最適な挙動を取るようになるため、ビルドスクリプトが最低限の記述で済むようになり、シンプルになる。Java プラグインであれば、

- プロダクションのソースコードは `src/main/java` に配置されていること
- プロダクションのリソースは `src/main/resources` に配置されていること 
- テストコードのソースコードは `src/test/java` に配置されていること
- テストコードのリソースは `src/test/resources` に配置されていること

などが自動的に適用される。挙動を変えたい場合は、規約に対応するプロパティを変更することで挙動を変えられる。例えば上記のソースコードの配置に関する規約は、ソースセットのプロパティ `sourceSets.<sourceSet>.java.srcDirs` や `sourceSets.<sourceSet>.resources.srcDirs` を変更することで実現できる。

## コンフィグレーションによる依存関係の変更

Gradle は依存関係をグループ化して分類するコンフィグレーションという仕組みがある。これによってコンパイル時には必要だが実行時には不要、といった依存関係も定義することができる。

Java プラグインは標準で次のコンフィグレーションを提供する。

- `compile`
- `runtime`
- `testCompile`
- `testRuntime`
- `archives`
	- このプロジェクトのアーティファクトを表す。デフォルトでは `jar` タスクの出力（JAR ファイル）のみを含む
- `default`
	- このプロジェクトに依存するプロジェクトが利用するデフォルトのクラスパス。コンフィグレーション `runtime` と `archives` の内容が含まれる。

## ソースセットの追加

ソースセットを追加すると、規約に従ってデフォルトでディレクトリが指定される。

```groovy
sourceSets {
  integrationTest
}
```

この場合は `src/integrationTest/java` と `src/integrationTest/resources` がソースコードディレクトリとして設定される。変更する場合はソースセットの `java.srcDir` プロパティを変更すればよい。

integrationTest のコンパイルクラスパスと実行時クラスパスを変更する場合は、以下のようになる。

```groovy
sourceSets {
  integrationTest {
    compileClasspath = sourceSets.main.output + configrations.testCompile // main の出力と testCompile のクラスパスを利用
    runtimeClasspath = output + compileClasspath // integrationTest の出力と、integrationTest のコンパイルクラスパスを実行時クラスパスとして利用
  }
}
```

ちなみに、ソースセットを追加すると自動的に以下のコンフィグレーションが追加される。

- `<SourceSet>Compile`
- `<SourceSet>Runtime`

さらに、ソースセットの追加は対応する `<SourceSet>CompileJava` タスクと `<SourceSet>ProcessResources` タスクが追加されるが、テストのタスクは追加されないので、インテグレーションテストタスクを別途定義する。

```groovy
task integrationTest(type: Test) {
  testClassesDir = sourceSets.integrationTest.output.classesDir
  classpath = sourceSets.integrationTest.runtimeClasspath // ソースセットの定義で設定したプロパティを integrationTest タスクのプロパティとして使用。
}
```