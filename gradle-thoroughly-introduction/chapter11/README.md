# Jenkins との連携

Jenkins の Gradle プラグインを使って Gradle を Jenkins 上で動作させることを解説した章。

Gradle プラグインを使った Jenkins ジョブの設定方法や設定項目の解説などが記載されている。この章もリファレンス的に手元に置いておき、必要な時に参照するのが良さそう。

* 環境変数や設定ファイルの準備
    * Env Inject プラグインを使ったり、Config File Provider プラグインを使って外部情報を inject/provide する方法
* シンプルな Java プロジェクト設定
    * JUnit のテスト結果
    * Javadoc
    * ビルド成果物
* コード検査（インスペクション）
* Violations プラグインを使って、コード検査を行う
    * Checkstyle
        * テストコードのコード規約チェックの除外方法
    * FindBugs
        * テストコードの潜在バグチェックの除外方法
    * Jacoco
        * テストコードのカバレッジ測定
    * JDepend
        * コードの依存性分析
    * PMD
        * 潜在エラーチェック
    * CodeNarc
        * Groovy のソースコード検査
    * Sonar

など。