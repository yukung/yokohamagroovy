# 他のビルドツールからの移行

## 移行ルート

#### Ant プロジェクトの場合

* Gradle の Ant 組み込み機能を使ってシームレスに Ant のプロパティやタスクを利用できる
* Ant スクリプトを Gradle スクリプトに取り込むことができる

#### Maven プロジェクトの場合

* `pom.xml` を変換して `build.gradle` を含む新規 Gradle プロジェクトを生成することができる

## Ant から Gradle へ

* Ant のビルドスクリプト `build.xml` を Gradle のビルドスクリプト `build.gradle` に取り込む
    * `ant.importBuild 'build.xml'` で取り込む
* Gradle のビルドスクリプト内で Ant のビルドスクリプトに定義してあるターゲットやプロパティを参照する
    * `build.xml` 内で定義されているターゲットを指定できる
    * `ant.properties.version` で `build.xml` 内で定義されている `version` プロパティを参照、上書きできる 
* Gradle のビルドスクリプト内で、Ant タスクを使用する
    * `ant.taskdef resource: <プロパティ>, classpath: configurations.<ant用クラスパスコンフィグレーション>.asPath` でタスクを読み込める

## Maven から Gradle へ

```console
$ cd my-maven-project
$ gradle init --type pom
```

`pom.xml` があるディレクトリで `gradle init --type pom` を実行すると対応した `build.gradle` を生成してくれる。