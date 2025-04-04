<script>
  import { onMount } from "svelte"

  export let foo

  const bar = 42
  let state = ""

  onMount(() => {
    state = "mounted"
  })

  const handleClick = () => {
    state = "clicked"
  }
</script>