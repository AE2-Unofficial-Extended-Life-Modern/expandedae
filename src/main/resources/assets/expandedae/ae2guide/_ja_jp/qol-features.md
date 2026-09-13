---
navigation:
  parent: expandedae-index.md
  title: QoL機能
  icon: expandedae:exp_pattern_provider
  position: 10
categories:
  - expandedae
---

# このMODは、以下にリストされているすべてのQoL機能を追加します。
## パターンプロバイダーのパターン乗算: パターンプロバイダーにボタンを追加し、含まれるすべてのパターンを乗算または除算できるようにします。
__乗数はスタックします！__

![modify_patterns.png](assets/modify_patterns.png)
## 追加のブロックモード: これにより、すべてのパターンプロバイダーに2つの追加のブロックモードが追加されます。
### デフォルト: これはAE2によるデフォルトのブロックモードです。接続されたストレージにこのパターンプロバイダーのパターン入力が含まれていない場合はパターンをプッシュし、ストレージにパターン入力ではないものが含まれている場合は、ブロックモードによってそれがスキップされてパターンがプッシュされます。
    
![blocking_1.png](assets/blocking/blocking_1.png)

### フル: このブロッキング モードでは、接続されたストレージに何かが含まれている場合、パターンはプッシュされません。 
    
![blocking_2.png](assets/blocking/blocking_2.png)

### スマート:ターゲットストレージにこの特定のパターンからの入力のみが含まれている場合、パターンプロバイダーが同じパターンをプッシュできるようにします。

![blocking_3.png](assets/blocking/blocking_3.png)