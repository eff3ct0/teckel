# Formal Language Definition

Teckel uses a specific set of language constructs to define data flows. Below is the formal syntax for this DSL:

```txt
Asset          := `Asset` <AssetRef> <Source>

Source         := <Input> | <Output> | <Transformation>
Input          := `Input` <Format> <Options> <SourceRef>
Output         := `Output` <AssetRef> <Format> <Options> <SourceRef>

// TODO: It need double-check and define correctly
Transformation ::= JoinOperation | GroupOperation | WindowOperation

// Join
JoinOperation  ::= `Join` <JoinType> <JoinRelation>
JoinType       ::= `Inner` | `Left` | `Right` | `Cross` | ...
JoinRelation   ::= `JoinRelation` <Source> <Source> [ <RelationField> ] 
RelationField  ::= `RelationField` <Column> <Column>

// Group
GroupOperation ::= `Group` <Source> <By> <Agg>
By             ::= `By` [Column]
Agg            ::= `Agg` [Column]

Select         ::= `Select` [Column]
Where          ::= `Where` [Column]

// Type Alias
AssetRef       := String
Format         := String
SourceRef      := String
Options        := `Map` String String
Context<T>     := `Map` <AssetRef> <T>
```
