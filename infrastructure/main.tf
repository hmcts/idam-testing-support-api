
locals {
  default_name = "${var.product}-${var.component}"
  tags = merge(
    var.common_tags,
    {
      "environment" = var.env == "idam-preview" && var.product == "idam" ? "idam-dev" : var.env
    },
  )
}

module "idam-testing-support-api-db" {
  count              = var.env == "idam-preview" ? 1 : 0
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = local.default_name
  location           = var.location
  env                = var.env
  subscription       = var.subscription
  postgresql_user    = "idamtestingsupportapi"
  database_name      = "idamtestingsupportapi"
  postgresql_version = 11
  sku_name           = "GP_Gen5_4"
  sku_tier           = "GeneralPurpose"
  sku_capacity       = "4"
  storage_mb         = "51200"
  common_tags        = local.tags
}
