terraform {
  backend "azurerm" {}
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.14.0"
    }
  }
}
provider "azurerm" {
  features {}
}

provider "azurerm" {
  subscription_id            = local.cft_vnet[local.env].subscription
  skip_provider_registration = "true"
  features {}
  alias = "cft_vnet"
}

locals {
  default_name   = "${var.product}-${var.component}"
  vault_name     = "${var.product}-${var.product}-${var.env}"
  instance_count = (var.env == "prod" || var.env == "idam-prod" || var.env == "idam-prod2") ? 0 : 1
  env_temp = replace(var.env, "idam-", "")
  env      = local.env_temp == "sandbox" ? "sbox" : local.env_temp
  cft_vnet = {
    sbox = {
      subscription = "b72ab7b7-723f-4b18-b6f6-03b0f2c6a1bb"
    }
    perftest = {
      subscription = "8a07fdcd-6abd-48b3-ad88-ff737a4b9e3c"
    }
    aat = {
      subscription = "96c274ce-846d-4e48-89a7-d528432298a7"
    }
    ithc = {
      subscription = "62864d44-5da9-4ae9-89e7-0cf33942fa09"
    }
    preview = {
      subscription = "8b6ea922-0862-443e-af15-6056e1c9b9a4"
    }
    demo = {
      subscription = "d025fece-ce99-4df2-b7a9-b649d3ff2060"
    }
    prod = {
      subscription = "8cbc6f36-7c56-4963-9d36-739db5d00b27"
    }
  }
}
