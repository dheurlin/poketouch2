
```kotlin
@Composable {
  
  val Emulator(
    onScreenChanged = {
      
    },
    onMoveSelected = {

    }
  )
}
```

eller:


```kotlin
onCreate() {
  val viewModel = ...
  val Emulator(onScreenChagned = )
  setContent {
    ...
  }
}
```

Jag tror allt bara har access till ViewModel
