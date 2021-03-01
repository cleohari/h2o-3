import h2o
import numpy as np
from h2o.estimators.gam import H2OGeneralizedAdditiveEstimator
from tests import pyunit_utils

def knots_error():
    # load and prepare California housing dataset
    np.random.seed(1234)
    data = h2o.H2OFrame(
        python_obj={'C1': list(np.random.randint(0, 9, size=1000)),
                    'target': list(np.random.randint(0, 2, size=1000))
                    })
    # use only 3 features and transform into classification problem
    feature_names = ['C1']
    data['target'] = data['target'].asfactor()
    # split into train and validation sets
    train, test = data.split_frame([0.8], seed=1234)
    # build the GAM model
    h2o_model = H2OGeneralizedAdditiveEstimator(family='binomial',
                                                gam_columns=feature_names,
                                                scale=[1],
                                                num_knots=[10],
                                                )
    try:
        h2o_model.train(x=feature_names, y='target', training_frame=train)
        assert False, "Number of knots validation should have failed"
    except Exception as ex:
        exception = str(ex)
        assert ("H2OModelBuilderIllegalArgumentException" in exception) and \
               (("has cardinality lower than the number of knots" in exception) or \
               ("does not have enough values to generate well-defined knots" in exception))
        print("Error correctly raised when cardinality < num_knots")

if __name__ == "__main__":
    h2o.init(ip='192.168.1.163', port=54321, strict_version_check=False)
    pyunit_utils.standalone_test(knots_error())
else:
    h2o.init(ip='192.168.1.163', port=54321, strict_version_check=False)
    knots_error()
