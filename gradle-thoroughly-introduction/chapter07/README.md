# 依存関係の管理

## 依存関係管理の目的

- 依存関係解決の自動化
- 推移的な依存関係の管理
- 依存関係の可視化

## 依存関係解決の自動化

### コンフィグレーション

- Maven のスコープのように依存関係を分類するグループ
- `configurations` ブロックで定義する

```gradle
configurations {
	conf1
}
```

### 依存関係の定義

- コンフィグレーションに対して割り当てる依存関係を定義する

```gradle
dependencies {
	conf1 files("libs/sample-lib.jar")
}
```

サポートしている依存関係の指定方法は以下。

- 外部モジュール依存関係
	- Maven や Ivy のリポジトリから取得する依存関係の指定
	- `'group:module:version'` か、`group: '<グループ名>', module: '<モジュール名>', version: '<バージョン>'` で指定
	- リポジトリは `repositories` で指定
- ファイル依存関係
	- `files()` や `fileTree()` でファイルパスをプロジェクトディレクトリからの相対パスで指定
	- `FileCollection` 型の値は全て依存関係に指定できる
- プロジェクト依存関係
	- マルチプロジェクト構成のプロジェクトで他のプロジェクトを指定する
	- `project()` でプロジェクトパスを指定
- Gradle API 依存関係
	- `gradleApi()` を呼ぶ
	- Gradle のプラグインやタスクを作成するときなど、Gradle 自体を拡張する時に使う
- ローカル Groovy 依存関係
	- `localGroovy()` を呼ぶ
	- Gradle を拡張する際に使う

### コンフィグレーションの継承

- 依存関係のコンフィグレーションは他のコンフィグレーションを継承できる
- `extendsFrom()` を使う
- テスト用のコンフィグレーションにプロダクション用のコンフィグレーションも含めたいといった時に使う

```gradle
configurations {
	conf1
	testConf1.extendsFrom conf1	// testConf1 は conf1 の依存関係も含む
}
```

### リポジトリ定義

#### Maven リポジトリ

`maven` ブロックを使って以下のようにリポジトリを設定する。

```gradle
repositories {
	maven {
		url "http://company-repositories.example.com/maven2"
	}
}
```

もう少し複雑な構成の場合（例えば POM と JAR ファイルの場所が分かれている場合など）は、以下のように指定する。

```gradle
repositories {
	maven {
		// POM ファイルはここを参照する
		url "http://company-repositories.example.com/maven2"
		// JAR ファイルは個々を参照する
		artifactUrls "http://company-repositories.example.com/jars"
		// こちらにも探しに行く
		artifactUrls "http://company-repositories.example.com/jars2"
	}
}
```

##### 注意点

```gradle
repositories {
	maven {
		url "http://company-repositories-1.example.com/maven2"
	}
	maven {
		url "http://company-repositories-2.example.com/maven2"
	}

}
```

上記のように複数のリポジトリを指定した場合は、上のリポジトリにある POM ファイルに定義された JAR ファイルが見つからなかった場合は、下のリポジトリにその JAR ファイルを探しに行くわけではない。リポジトリ定義は1つずつ**独立**しているので注意。

##### Maven Central リポジトリ

- `mavenCentral()` を呼ぶだけでよい。

```gradle
repositories {
	mavenCentral()
}
```

##### Maven Local リポジトリ

- `mavenLocal()` を呼ぶだけでよい。

```gradle
repositories {
	mavenLocal()
}
```

##### JCenter Maven リポジトリ

- `jcenter()` を呼ぶだけでよい。

```gradle
repositories {
	jcenter()
}
```

#### Ivy リポジトリ

`ivy` ブロックをを使って以下のようにリポジトリを設定する。

```gradle
	ivy {
		url "http://example.com/ivy-repo"	// Ivy リポジトリの場所
		layout "maven"	// Ivy リポジトリの構造
	}
```

- Ivy は以下の様な属性を使って細かくファイルを配置するパスを変えられる
	- `organisation`
	- `module`
	- `revision`
	- `artifact`
	- `revision`
	- `classifier`
	- `ext`
- `layout` は以下のオプションが選択できる
	- `gradle`
		- メタデータ
			- `URL/organisation/module/revision/ivy-revision.xml`
		- 成果物
			- `URL/organisation/module/revision/artifact-revision(-classifier)(.ext)`
	- `maven`
		- Ivy の organisation の部分が、Maven の `groupId` に置き換わる。`.` が `/` に置換される
	- `pattern`

```gradle
// pattern
repositories {
	ivy {
		url "http://example.com/ivy-repo"	// Ivy リポジトリの場所
		layout 'pattern', {
			artifact '[organisation]/[module]/[artifact](.[ext])' // アーティファクトの場所
			ivy '[organisation]/[module]/[revision]/ivy.xml' //ivy.xml の場所
			m2compatible = true // Maven 互換、つまり [organisation] 部分をサブディレクトリに分割するかどうか
		}
	}
}
```

#### パスワードで保護されたリポジトリ

- BASIC 認証で保護された Maven リポジトリまたは Ivy リポジトリにアクセスするには、`credentials` ブロックを利用して次のように認証情報を記述する。

```gradle
repositories {
	maven {
		url "http://repo.example.com/maven2"
		credentials {
			username 'user'
			password 'password'
		}
	}
	ivy {
		url "http://repo.example.com/ivy"
		credentials {
			username 'user'
			password 'password'
		}
	}

}
```

なお、認証情報はビルドスクリプトに直書きするのではなく、ユーザーのホームディレクトリの `.gradle/` に `gradle.properties` を配置してその中にプロジェクトプロパティとして記述する方法や、`-P` でコマンドライン引数として認証情報を渡す方がよい。

#### フラットディレクトリリポジトリ

- ローカルファイルシステム上の単純なディレクトリをリポジトリとして指定できる
- バージョン管理システムに JAR を上げている場合や、社内の共有ディレクトリに配置している場合など、従来の運用との整合性を取らざるをえない場合などに使う

```gradle
repositories {
	flatDir {
		dirs "libs", "doc-repo"
	}
}

dependencies {
	conf1 group: "org.slf4j", name: "slf4j-api", version: "1.7.5"
	conf1 name: "index", ext: "html"	// doc-repo/index.html を依存関係として設定。フラットディレクトリなので、group ごとのサブディレクトリは作ることが出来ず、group 指定も不要。
}
```

### 動的バージョンと変更性モジュール

- `1.7.+`
	- 1.7 系のうち、最新版
- `latest.integration`
	- 不安定版も含めて最新版
- `latest.release`
	- 安定版の最新版
- Maven の `-SNAPSHOT` バージョンや Ivy の `changing` 属性が有効になっているモジュールは、Gradle が24時間キャッシュをしてそれ以降はリポジトリに問い合わせに行く
	- 時間を調整したい場合は、以下のように記述する

```gradle
configurations {
	conf1
}
// conf1 コンフィグレーションの動的バージョンのキャッシュ間隔を調整
configurations.conf1.resolutionStrategy.cacheDynamicVersionsFor 1, 'minutes'
// conf1 コンフィグレーションの変更性モジュールのキャッシュ間隔を調整
configurations.conf1.resolutionStrategy.cacheChangingModulesFor 5, 'hours'
```

###### 概念

- ivy のステータススキームを利用している
	- `integration`, `milestone`, `release`
- Maven の `-SNAPSHOT` は `integration`、それ以外は `release` に振り分けられる

## 推移的な依存関係の管理

- 明示的に指定した依存関係からさらに再帰的に解決される依存関係のこと

#### 推移的依存関係とコンフィグレーション

```gradle
// geregere/build.gradle

group = "geregere"
version = 0.1

configurations {
	// default コンフィグレーションは存在しないため、yoreyore プロジェクトのビルドが明示的に conf1 を指定しない限り、依存関係を解決できない
	conf1
	conf2
}

dependencies {
	conf1 group: 'com.example', name: 'aaa', version: '0.1'
	conf2 group: 'com.example', name: 'bbb', version: '0.1'
}
```

```gradle
// yoreyore/build.gradle

repositories {
	ivy {
		url "http://localhost:8000"
			layout 'pattern' , {
				artifact '[organisation]/[module]/[artifact]-[revision](.[ext])'
				ivy '[organisation]/[module]/ivy.xml'
			}
	}
}

configurations {
	compile
}

dependencies {
	compile group: 'geregere', name: 'geregere', version: '0.1', configuration: "conf1" // 明示的に指定しないと、"default" コンフィグレーションが指定されたものとする
}

task showDeps << {
	configurations.compile.each {
		println it.absolutePath
	}
}
```

### 競合の解決

クラスパスに同じモジュールの別のバージョンが衝突した時、Gradle は以下の2つの戦略に沿って競合を解決する

- Newest 戦略
	- 最も新しいバージョンの依存関係を使用する（デフォルト）
- Fail 戦略
	- 例外を発行してビルドを失敗させる

```gradle
configurations.testConf1 {
	resolutionStrategy {
		failOnVersionConflict()	// Fail 戦略を採用する
	}
}
```

### 推移的な依存関係の除外設定

使用しているモジュールが要求する推移的な依存関係を無視するように設定することができる。

- `exclude()` を使う
- `module` で無視するモジュールを指定する
- `group` で特定のグループごと除外することもできる

```gradle
dependencies {
	conf1 group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.3.1'
	testConf1(group: 'org.spockframework', name: 'spock-core', version: '0.7-groovy-2.0') {
		exclude module: 'groovy-all' // groovy-all は無視する
	}
}
```

### 使用するバージョンの強制

使用したいモジュールのバージョンが決まっている場合は、コンフィグレーション内で特定のバージョンを強制的に使用するように設定できる。

```gradle
configurations.testConf1 {
	resolutionStrategy {
		failOnVersionConflict()
		force 'org.hamcrest:hamcrest-core:1.3' // testConf1 で Hamcrest が要求されたら必ずバージョン 1.3 を使う
	}
}
dependencies {
	conf1(group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.3.1') {
		force = true // バージョンが競合したら必ずこの依存関係を使う
	}
	testConf1(group: 'org.spockframework', name: 'spock-core', version: '0.7-groovy-2.0')
}
```

### クライアントモジュール依存関係

推移的な依存関係の解決は、外部リポジトリに格納されているモジュールのメタデータを使用するが、Gradle ではメタデータを利用せずにビルドスクリプト内で直接記述することも可能。

これをクライアントモジュール依存関係と呼ぶ。

```gradle
repositories {
	flatDir {
		dirs "libs"
	}
}
configurations {
	conf1
	testConf1.extendsFrom conf1
}
dependencies {
	conf1 group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.3.1'
	// クライアントモジュール設定
	testConf1 module('org.spockframework:spock-core:0.7-groovy-2.0') {
		dependency 'org.codehaus.groovy:groovy-all:2.3.1'
		dependency 'org.hamcrest:hamcrest-core:1.3'
		module(group: 'junit', name: 'junit-dep', version: '4.10') {
			dependency group: 'org.hamcrest', name: 'hamcrest-core', version: '1.3'
		}
	}
}
```

これの用途は、主に2つ。

- 依存しているモジュールのメタデータが誤っていたり、プロジェクトにとって都合が悪かったりした場合の対処
- 必要なモジュールをローカルのファイルシステムに格納して運用している際に、依存関係を適切に管理するため
	- こうしておくことで、例えば依存ライブラリを削除しようとした時に共に依存しているライブラリも把握することができる

## 使用しているモジュールの調査

`dependencies` タスクを使う。

```shell-session
$ gradle dependencies
:dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

conf1
\--- org.codehaus.groovy:groovy-all:2.3.1

testConf1
Download https://repo1.maven.org/maven2/org/spockframework/spock-core/0.7-groovy-2.0/spock-core-0.7-groovy-2.0.pom
Download https://repo1.maven.org/maven2/junit/junit-dep/4.10/junit-dep-4.10.pom
+--- org.codehaus.groovy:groovy-all:2.3.1
\--- org.spockframework:spock-core:0.7-groovy-2.0
     +--- junit:junit-dep:4.10
     |    \--- org.hamcrest:hamcrest-core:1.1 -> 1.3
     +--- org.codehaus.groovy:groovy-all:2.0.5 -> 2.3.1
     \--- org.hamcrest:hamcrest-core:1.3

(*) - dependencies omitted (listed previously)

BUILD SUCCESSFUL

Total time: 3.108 secs
```

## キャッシュ制御とオフライン実行

- Gradle は一度解決した依存関係をローカルにキャッシュする
- ただし、以下の条件に該当する場合はネットワークへのアクセスが発生する可能性がある
	- `repositories` ブロックで、ネットワーク上にあるリポジトリが定義されている
	- 依存関係のうち、「外部モジュール依存関係」が使用されている
- 同じモジュールの依存関係でも、違うリポジトリを使用した場合はネットワークアクセスが発生する
	- Gradle はそれをハッシュベースのファイルキャッシュを使って解決する
	- Gradle のキャッシュディレクトリだけでなく、Maven のローカルリポジトリ（`~/.m2/repository`）にあるファイルも対象

## オフライン実行

- ネットワークアクセスを避け、オフライン環境でビルドしたい場合は、`--offline` オプションを付けてビルドを実行する

```shell-session
$ gradle --offline showDeps
```

完全に閉鎖環境下で開発する必要がある場合、以下の運用にする必要がある。

- ローカルマシンやイントラネット内にリポジトリサーバを立てる
	- `Apache Archiva`, `Artifactory`, `Nexus` などを使う
- 外部リポジトリと外部モジュール依存関係を利用しない
	- フラットディレクトリリポジトリやファイル依存関係を使って依存関係を管理する